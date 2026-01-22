package com.example.note;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.note.network.Message;
import com.example.note.network.OpenAIApi;
import com.example.note.network.OpenAIRequest;
import com.example.note.network.OpenAIResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Implementasikan listener dari BottomSheet
public class DetailNoteActivity extends AppCompatActivity implements ExplanationBottomSheetFragment.BottomSheetListener {

    private TextView tvDetailTitle, tvDetailContent;
    private ImageButton btnEditTitle;
    private int noteId = -1;
    private int categoryId = -1;
    private String categoryName = "";
    private NoteRepository noteRepository;

    private LinearLayout searchBar;
    private EditText etSearch;
    private ImageButton btnNext, btnPrev;
    private String fullText = "";
    private ArrayList<Integer> searchResultIndices = new ArrayList<>();
    private int currentSearchIndex = -1;

    private Button btnGoToFlashcard, btnGoToQuiz;

    // Deklarasi tombol-tombol FAB
    private FloatingActionButton btnScrollTop;
    private FloatingActionButton btnSummarizeAi;
    private FloatingActionButton btnExplainAi;

    private AlertDialog loadingDialog;
    private OpenAIApi openAIApi;

    private final ActivityResultLauncher<Intent> editNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadNoteDetails();
                    Toast.makeText(this, "Data telah disinkronkan.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_note);

        // --- Inisialisasi View ---
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        btnEditTitle = findViewById(R.id.btnEditTitle);
        searchBar = findViewById(R.id.searchBar);
        etSearch = findViewById(R.id.etSearch);
        btnGoToFlashcard = findViewById(R.id.btnGoToFlashcard);
        btnGoToQuiz = findViewById(R.id.btnGoToQuiz);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);

        // Inisialisasi FABs
        btnScrollTop = findViewById(R.id.btnScrollTop);
        btnSummarizeAi = findViewById(R.id.btnSummarizeAi);
        btnExplainAi = findViewById(R.id.btnExplainAi);

        // Gunakan NestedScrollView
        NestedScrollView scrollView = findViewById(R.id.scrollContent);

        MaterialToolbar topAppBarDetail = findViewById(R.id.topAppBarDetail);
        setSupportActionBar(topAppBarDetail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        noteRepository = new NoteRepository(this);
        setupOpenAI();
        noteId = getIntent().getIntExtra("note_id", -1);
        loadNoteDetails();

        topAppBarDetail.setNavigationOnClickListener(v -> finish());

        btnSummarizeAi.show();
        btnExplainAi.show();
        btnScrollTop.hide();

        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollYValue) -> {
            if (scrollY > 300) {
                btnScrollTop.show();
            }
            else if (scrollY < 300) {
                btnScrollTop.hide();
            }
        });


        // Pindahkan semua setup listener ke fungsi terpisah
        setupClickListeners();
    }

    private void setupClickListeners() {
        btnEditTitle.setOnClickListener(v -> {
            Intent editIntent = new Intent(DetailNoteActivity.this, AddNoteActivity.class);
            editIntent.putExtra("isEdit", true);
            editIntent.putExtra("id", noteId);
            editIntent.putExtra("title", tvDetailTitle.getText().toString());
            editIntent.putExtra("content", fullText);
            editIntent.putExtra("categoryId", categoryId);
            editIntent.putExtra("categoryName", categoryName);
            editNoteLauncher.launch(editIntent);
        });

        // Listener untuk tombol FAB
        btnSummarizeAi.setOnClickListener(v -> {
            String content = tvDetailContent.getText().toString();
            if (content.length() < 50) {
                Toast.makeText(this, "Konten terlalu pendek untuk diringkas.", Toast.LENGTH_SHORT).show();
                return;
            }
            generateAiContent(content, "summarize");
        });

        btnExplainAi.setOnClickListener(v -> {
            String content = tvDetailContent.getText().toString();
            if (content.length() < 20) {
                Toast.makeText(this, "Konten terlalu pendek untuk dijelaskan.", Toast.LENGTH_SHORT).show();
                return;
            }
            generateAiContent(content, "explain_simple");
        });

        btnScrollTop.setOnClickListener(v -> {
            NestedScrollView scrollView = findViewById(R.id.scrollContent);
            scrollView.smoothScrollTo(0, 0);
        });

        // Listener untuk tombol di bawah
        btnGoToFlashcard.setOnClickListener(v -> navigateToTab(R.id.navigation_flashcard));
        btnGoToQuiz.setOnClickListener(v -> navigateToTab(R.id.navigation_quiz));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                highlightText(etSearch.getText().toString());
                return true;
            }
            return false;
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { highlightText(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnNext.setOnClickListener(v -> navigateSearchResults(true));
        btnPrev.setOnClickListener(v -> navigateSearchResults(false));
    }


    private void loadNoteDetails() {
        if (noteId == -1) {
            Toast.makeText(this, "ID Catatan tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        noteRepository.getNoteById(noteId, new Callback<Note>() {
            @Override
            public void onResponse(@NonNull Call<Note> call, @NonNull Response<Note> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Note note = response.body();
                    tvDetailTitle.setText(note.getTitle());
                    tvDetailContent.setText(note.getContent());
                    fullText = note.getContent();
                    categoryId = note.getCategoryId();
                    categoryName = note.getCategoryName();
                } else {
                    Toast.makeText(DetailNoteActivity.this, "Gagal memuat detail catatan.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Note> call, @NonNull Throwable t) {
                Toast.makeText(DetailNoteActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overflow_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_search_detail) {
            toggleSearchBar();
            return true;
        } else if (itemId == R.id.action_more_detail) {
            showOverflowMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showOverflowMenu() {
        View menuView = LayoutInflater.from(this).inflate(R.layout.menu_overflow_icons, null);
        TextView btnMenuDelete = menuView.findViewById(R.id.btnMenuDelete);
        TextView btnMenuInfo = menuView.findViewById(R.id.btnMenuInfo);

        // Sembunyikan tombol yang sudah dipindah
        menuView.findViewById(R.id.btnMenuSummarize).setVisibility(View.GONE);
        menuView.findViewById(R.id.btnMenuExplainSimple).setVisibility(View.GONE);

        final PopupWindow popupWindow = new PopupWindow(menuView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        MaterialToolbar topAppBarDetail = findViewById(R.id.topAppBarDetail);
        View anchor = topAppBarDetail.findViewById(R.id.action_more_detail);
        if (anchor == null) anchor = topAppBarDetail;
        popupWindow.showAsDropDown(anchor);

        btnMenuDelete.setOnClickListener(v -> {
            popupWindow.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Catatan")
                    .setMessage("Apakah Anda yakin ingin menghapus catatan ini?")
                    .setPositiveButton("Hapus", (dialog, which) -> deleteNote())
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnMenuInfo.setOnClickListener(v -> {
            popupWindow.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Info Catatan")
                    .setMessage("Fitur ini sedang dalam pengembangan.")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void deleteNote() {
        noteRepository.deleteNote(noteId, new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailNoteActivity.this, "Catatan dihapus", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(DetailNoteActivity.this, "Gagal menghapus", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(DetailNoteActivity.this, "Error koneksi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAiContent(String textContent, String mode) {
        showLoadingDialog();
        String prompt;
        String bottomSheetTitle;
        boolean showApplyButton;

        if ("explain_simple".equals(mode)) {
            prompt = "Anda adalah seorang asisten belajar yang ahli. Berdasarkan teks berikut, berikan dua jenis penjelasan dalam satu jawaban:\n\n1. **Analogi:** Jelaskan konsep utama dari teks menggunakan analogi yang mudah dipahami.\n\n2. **Poin Kunci:** Identifikasi dan jabarkan 3 poin paling penting dari teks dalam format daftar.\n\nTeks Asli:\n" + textContent;
            bottomSheetTitle = "Penjelasan & Poin Kunci";
            showApplyButton = false;
        } else { // "summarize"
            prompt = "Anda adalah seorang penulis teknis yang ahli. Berdasarkan teks mentah berikut, kembangkan atau ringkas menjadi penjelasan yang jelas, terstruktur, dan komprehensif. KEMBALIKAN HANYA HASIL AKHIRNYA.\n\nTeks Asli:\n" + textContent;
            bottomSheetTitle = "Hasil Ringkasan AI";
            showApplyButton = true;
        }

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));
        OpenAIRequest request = new OpenAIRequest("openai/gpt-3.5-turbo", messages, 0.7);

        openAIApi.createChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenAIResponse> call, @NonNull Response<OpenAIResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String resultText = response.body().choices.get(0).message.getContent().trim();
                    ExplanationBottomSheetFragment bottomSheet = ExplanationBottomSheetFragment.newInstance(bottomSheetTitle, resultText, showApplyButton);
                    bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenAIResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(DetailNoteActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApplyClicked(String text) {
        tvDetailContent.setText(text);
        fullText = text;
        Note noteToUpdate = new Note();
        noteToUpdate.setTitle(tvDetailTitle.getText().toString());
        noteToUpdate.setContent(text);
        noteToUpdate.setCategoryId(categoryId);

        noteRepository.updateNote(noteId, noteToUpdate, new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DetailNoteActivity.this, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DetailNoteActivity.this, "Gagal menyimpan perubahan ke server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(DetailNoteActivity.this, "Error koneksi saat menyimpan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleSearchBar() {
        if (searchBar.getVisibility() == View.VISIBLE) {
            searchBar.setVisibility(View.GONE);
            tvDetailContent.setText(fullText); // Kembalikan teks asli
        } else {
            searchBar.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        }
    }

    private void highlightText(String query) {
        if (fullText == null || fullText.isEmpty()) return;

        // Hapus highlight sebelumnya dan reset
        tvDetailContent.setText(fullText);
        searchResultIndices.clear();
        currentSearchIndex = -1;

        if (query == null || query.isEmpty()) {
            return; // Jika query kosong, cukup reset dan keluar
        }

        String content = fullText.toLowerCase();
        query = query.toLowerCase();
        SpannableString spannableString = new SpannableString(fullText);

        int startIndex = content.indexOf(query);
        while (startIndex >= 0) {
            searchResultIndices.add(startIndex);
            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), startIndex, startIndex + query.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            startIndex = content.indexOf(query, startIndex + query.length());
        }

        tvDetailContent.setText(spannableString);

        if (!searchResultIndices.isEmpty()) {
            currentSearchIndex = 0;
            // Setelah highlight, langsung scroll ke hasil pertama
            scrollToResult(currentSearchIndex);
        }
    }

    // 🔥🔥 FUNGSI UTAMA PERBAIKAN DI SINI 🔥🔥
    private void navigateSearchResults(boolean isNext) {
        if (searchResultIndices.isEmpty()) {
            Toast.makeText(this, "Tidak ada hasil pencarian.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isNext) {
            currentSearchIndex = (currentSearchIndex + 1) % searchResultIndices.size();
        } else {
            currentSearchIndex = (currentSearchIndex - 1 + searchResultIndices.size()) % searchResultIndices.size();
        }

        scrollToResult(currentSearchIndex);
    }

    // Fungsi helper baru untuk melakukan scroll
    private void scrollToResult(int index) {
        if (index < 0 || index >= searchResultIndices.size()) return;

        NestedScrollView scrollView = findViewById(R.id.scrollContent);
        int position = searchResultIndices.get(index);

        // Dapatkan layout dari TextView untuk menghitung posisi baris
        Layout layout = tvDetailContent.getLayout();
        if (layout == null) return; // Jika layout belum siap, hentikan

        int line = layout.getLineForOffset(position);

        // Dapatkan posisi Y dari baris tersebut
        Rect bounds = new Rect();
        layout.getLineBounds(line, bounds);
        int y = bounds.top;

        // Lakukan scroll ke posisi Y yang dihitung
        scrollView.smoothScrollTo(0, y);

        // Beri tahu pengguna hasil ke berapa yang sedang ditampilkan
        Toast.makeText(this, "Hasil " + (index + 1) + " dari " + searchResultIndices.size(), Toast.LENGTH_SHORT).show();
    }

    private void navigateToTab(int destinationTabId) {
        String content = tvDetailContent.getText().toString();
        if (content.length() < 50) {
            Toast.makeText(this, "Konten catatan terlalu pendek untuk diuji.", Toast.LENGTH_SHORT).show();
            return;
        }
        MainActivity.contentToProcessFromExternal = content;
        MainActivity.destinationTabFromExternal = destinationTabId;
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    private void setupOpenAI() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                    .header("HTTP-Referer", getPackageName())
                    .header("X-Title", getString(R.string.app_name));
            okhttp3.Request request = requestBuilder.build();
            return chain.proceed(request);
        });
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openrouter.ai/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        openAIApi = retrofit.create(OpenAIApi.class);
    }

    private void showLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void handleApiError(Response<?> response) {
        String errorBody = "Gagal memuat detail kesalahan.";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        new AlertDialog.Builder(this)
                .setTitle("Gagal Mendapatkan Respons")
                .setMessage("Kode Error: " + response.code() + "\n\nPesan:\n" + errorBody)
                .setPositiveButton("OK", null)
                .show();
    }
}

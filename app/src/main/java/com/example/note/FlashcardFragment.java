package com.example.note;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.note.network.FlashcardJsonItem;
import com.example.note.network.Message;
import com.example.note.network.OpenAIApi;
import com.example.note.network.OpenAIRequest;
import com.example.note.network.OpenAIResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FlashcardFragment extends Fragment {

    private Spinner spinnerNotes;
    private TextView tvFlashcardText, tvHintFlip;
    private View cardFlashcard;
    private Button btnGenerateAi;

    private List<Flashcard> flashcards = new ArrayList<>();
    private int currentIndex = 0;
    private boolean showingAnswer = false;
    private NoteRepository noteRepository;
    private List<Note> filteredNotes = new ArrayList<>();
    private Note currentNote = null;

    private GestureDetector gestureDetector;
    private AlertDialog loadingDialog;
    private OpenAIApi openAIApi;

    private static final String SPINNER_PLACEHOLDER = "Pilih Catatan untuk Flashcard";

    // 🔥🔥 PERBAIKAN 1: Tambahkan AnimatorSet
    private AnimatorSet frontAnim, backAnim;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flashcard, container, false);

        spinnerNotes = view.findViewById(R.id.spinnerNotes);
        tvFlashcardText = view.findViewById(R.id.tvFlashcardText);
        cardFlashcard = view.findViewById(R.id.cardFlashcard);
        View btnPrev = view.findViewById(R.id.btnPrev);
        View btnNext = view.findViewById(R.id.btnNext);
        btnGenerateAi = view.findViewById(R.id.btnGenerateAi);
        tvHintFlip = view.findViewById(R.id.tvHintFlip);

        noteRepository = new NoteRepository(requireContext());
        setupOpenAI();

        // 🔥🔥 PERBAIKAN 2: Setup animasi flip
        setupFlipAnimation();


        cardFlashcard.setOnClickListener(v -> flipCard());
        btnPrev.setOnClickListener(v -> prevFlashcardWithAnimation());
        btnNext.setOnClickListener(v -> nextFlashcardWithAnimation());
        setupGestureDetector();
        cardFlashcard.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // <-- Pastikan ini 'true' agar onClick juga bisa berjalan
        });

        return view;
    }

    // ... (Fungsi onViewCreated dan lainnya TIDAK BERUBAH) ...

    // 🔥🔥 PERBAIKAN 3: Fungsi untuk setup animasi
    private void setupFlipAnimation() {
        float scale = getResources().getDisplayMetrics().density;
        cardFlashcard.setCameraDistance(8000 * scale);

        frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_in);
        backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_in);
    }

    // 🔥🔥 PERBAIKAN 4: Implementasi fungsi flipCard()
    private void flipCard() {
        if (flashcards.isEmpty()) {
            Toast.makeText(getContext(), "Buat flashcard terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        showingAnswer = !showingAnswer;

        if (showingAnswer) {
            frontAnim.setTarget(cardFlashcard);
            frontAnim.start();
        } else {
            backAnim.setTarget(cardFlashcard);
            backAnim.start();
        }

        // Post-delayed untuk mengubah teks di tengah-tengah animasi
        cardFlashcard.postDelayed(this::showFlashcard, 150);
    }

    private void showFlashcard() {
        if (flashcards.isEmpty() || currentIndex >= flashcards.size()) {
            tvFlashcardText.setText("Pilih catatan dan klik 'Buat Flashcard dengan AI'");
            tvHintFlip.setVisibility(View.GONE);
            return;
        }

        Flashcard currentFlashcard = flashcards.get(currentIndex);
        if (showingAnswer) {
            tvFlashcardText.setText(currentFlashcard.getAnswer());
        } else {
            tvFlashcardText.setText(currentFlashcard.getQuestion());
        }
        tvHintFlip.setVisibility(View.VISIBLE);
    }

    private void prevFlashcardWithAnimation() {
        if (currentIndex > 0) {
            currentIndex--;
            showingAnswer = false; // Selalu kembali ke pertanyaan
            showFlashcard();
        } else {
            Toast.makeText(getContext(), "Ini adalah kartu pertama", Toast.LENGTH_SHORT).show();
        }
    }

    private void nextFlashcardWithAnimation() {
        if (currentIndex < flashcards.size() - 1) {
            currentIndex++;
            showingAnswer = false; // Selalu kembali ke pertanyaan
            showFlashcard();
        } else {
            Toast.makeText(getContext(), "Ini adalah kartu terakhir", Toast.LENGTH_SHORT).show();
        }
    }

    private void processJsonResponse(String jsonResponse) {
        // ... (Kode parsing JSON Anda tidak berubah)
        try {
            Gson gson = new Gson();
            Type flashcardListType = new TypeToken<ArrayList<FlashcardJsonItem>>(){}.getType();
            List<FlashcardJsonItem> jsonItems = gson.fromJson(jsonResponse, flashcardListType);

            if (jsonItems == null || jsonItems.isEmpty()) {
                tvFlashcardText.setText("AI tidak dapat membuat flashcard dari teks ini.");
                tvHintFlip.setVisibility(View.GONE);
                return;
            }

            flashcards.clear();
            for (FlashcardJsonItem item : jsonItems) {
                if (item.question != null && item.answer != null) {
                    flashcards.add(new Flashcard(item.question, item.answer));
                }
            }

            if (!flashcards.isEmpty()) {
                currentIndex = 0;
                showingAnswer = false;
                showFlashcard(); // Langsung tampilkan kartu pertama
                Toast.makeText(getContext(), flashcards.size() + " flashcard berhasil dibuat.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "AI tidak dapat membuat flashcard dari respons.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            tvFlashcardText.setText("Gagal memproses respons AI. Coba lagi.");
            Toast.makeText(getContext(), "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX > 0) {
                        prevFlashcardWithAnimation(); // Swipe ke kanan (sebelumnya)
                    } else {
                        nextFlashcardWithAnimation(); // Swipe ke kiri (berikutnya)
                    }
                    return true;
                }
                return false;
            }
        });
    }

    // ... (Sisa fungsi Anda seperti onResume, onViewCreated, loadNotesToSpinner, generateFlashcardsWithAI, dll, tidak perlu diubah)

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String contentToProcess = null;

        if (args != null && args.containsKey("NOTE_CONTENT_FROM_DETAIL")) {
            contentToProcess = args.getString("NOTE_CONTENT_FROM_DETAIL");
        }

        if (contentToProcess != null && !contentToProcess.isEmpty()) {
            spinnerNotes.setVisibility(View.GONE);
            btnGenerateAi.setVisibility(View.GONE);
            generateFlashcardsWithAI(contentToProcess);
        } else {
            spinnerNotes.setVisibility(View.VISIBLE);
            btnGenerateAi.setVisibility(View.VISIBLE);
            loadNotesToSpinner();
            btnGenerateAi.setOnClickListener(v -> {
                if (currentNote != null && currentNote.getContent() != null && !currentNote.getContent().trim().isEmpty()) {
                    generateFlashcardsWithAI(currentNote.getContent());
                } else {
                    Toast.makeText(getContext(), "Pilih catatan yang valid dari daftar.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadNotesToSpinner() {
        noteRepository.getAllNotes(new Callback<List<Note>>() {
            @Override
            public void onResponse(@NonNull Call<List<Note>> call, @NonNull Response<List<Note>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    filteredNotes.clear();
                    for (Note note : response.body()) {
                        if (note.getContent() != null && note.getContent().length() >= 50) {
                            filteredNotes.add(note);
                        }
                    }

                    if (filteredNotes.isEmpty()) {
                        tvFlashcardText.setText("Tidak ada catatan yang bisa dijadikan flashcard.\n(Minimal 50 karakter)");
                        spinnerNotes.setVisibility(View.GONE);
                        btnGenerateAi.setVisibility(View.GONE);
                        return;
                    }

                    List<String> noteTitles = new ArrayList<>();
                    noteTitles.add(SPINNER_PLACEHOLDER);
                    for (Note note : filteredNotes) {
                        noteTitles.add(note.getTitle());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.spinner_item_custom, noteTitles) {
                        @Override
                        public boolean isEnabled(int position) {
                            return position != 0;
                        }

                        @Override
                        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            if (position == 0) {
                                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                            } else {
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.popup_menu_text_color));
                            }
                            return view;
                        }
                    };

                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
                    spinnerNotes.setAdapter(adapter);

                    spinnerNotes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                currentNote = null;
                                flashcards.clear();
                                showFlashcard();
                            } else {
                                currentNote = filteredNotes.get(position - 1);
                                flashcards.clear();
                                tvFlashcardText.setText("Catatan '" + currentNote.getTitle() + "' dipilih.\nKlik 'Buat Flashcard dengan AI' untuk memulai.");
                                tvHintFlip.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            currentNote = null;
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Gagal memuat catatan dari server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Note>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void generateFlashcardsWithAI(String textContent) {
        showLoadingDialog();
        String promptText = "Anda adalah seorang guru yang membuat soal ujian dari sebuah teks. " +
                "Berdasarkan teks berikut, buatlah daftar pasangan pertanyaan dan jawaban. " +
                "KEMBALIKAN HANYA ARRAY JSON, tanpa teks penjelasan tambahan, tanpa 'json', tanpa komentar. " +
                "Setiap objek dalam array harus memiliki kunci 'question' dan 'answer'. " +
                "Pastikan pertanyaan menguji konsep-konsep penting dari teks.\n\nTeks asli:\n" + textContent;

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message("user", promptText));
        OpenAIRequest request = new OpenAIRequest("openai/gpt-3.5-turbo", messages, 0.5);

        openAIApi.createChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(Call<OpenAIResponse> call, Response<OpenAIResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String jsonResponse = response.body().choices.get(0).message.getContent();
                    processJsonResponse(jsonResponse);
                } else {
                    handleApiError(response);
                }
            }
            @Override
            public void onFailure(Call<OpenAIResponse> call, Throwable t) {
                hideLoadingDialog();
                if (getContext() != null)
                    Toast.makeText(getContext(), "Gagal terhubung: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupOpenAI() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                    .header("HTTP-Referer", requireActivity().getPackageName())
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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

    private void handleApiError(Response<OpenAIResponse> response) {
        String errorBody = "Tidak ada detail kesalahan.";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Gagal Mendapatkan Respons")
                .setMessage("Kode Error: " + response.code() + "\n\nPesan:\n" + errorBody)
                .setPositiveButton("OK", null)
                .show();
    }
}

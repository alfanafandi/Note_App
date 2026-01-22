package com.example.note;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.note.network.Message;
import com.example.note.network.OpenAIApi;
import com.example.note.network.OpenAIRequest;
import com.example.note.network.OpenAIResponse;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSave;
    private ImageButton btnBack, btnCategory;
    private Button btnFillWithAi;
    private AlertDialog loadingDialog;
    private OpenAIApi openAIApi;

    // --- PERUBAHAN UTAMA ---
    private NoteRepository noteRepository; // <-- GANTI DENGAN INI
    // private DBHelper dbHelper; // <-- HAPUS INI

    private boolean isEditMode = false;
    private int noteId = -1;
    private int selectedCategoryId = -1;
    private String selectedCategoryName = "";
    private String autoCategoryNameFromInspiration = null;

    private final ActivityResultLauncher<Intent> categoryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedCategoryId = result.getData().getIntExtra("selectedCategoryId", -1);
                    selectedCategoryName = result.getData().getStringExtra("selectedCategoryName");
                    autoCategoryNameFromInspiration = null;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnCategory = findViewById(R.id.btnCategory);
        btnFillWithAi = findViewById(R.id.btnFillWithAi);

        // --- GANTI DBHelper DENGAN NoteRepository ---
        noteRepository = new NoteRepository(this); // GANTI DENGAN INI
        // dbHelper = new DBHelper(this); // HAPUS INI

        setupOpenAI();

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEdit", false);

        if (isEditMode) {
            handleEditMode(intent);
        } else {
            handleNewNoteMode(intent);
        }

        btnBack.setOnClickListener(v -> finish());

        btnCategory.setOnClickListener(v -> {
            Intent catIntent = new Intent(AddNoteActivity.this, CategoryActivity.class);
            catIntent.putExtra("mode", "addnote");
            categoryLauncher.launch(catIntent);
        });

        btnSave.setOnClickListener(v -> saveOrUpdateNote());

        btnFillWithAi.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                generateContentWithAI(title);
            } else {
                Toast.makeText(this, "Judul tidak boleh kosong untuk menggunakan AI", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleEditMode(Intent intent) {
        btnSave.setText("Update Catatan");
        noteId = intent.getIntExtra("id", -1);
        etTitle.setText(intent.getStringExtra("title"));
        etContent.setText(intent.getStringExtra("content"));
        selectedCategoryId = intent.getIntExtra("categoryId", -1);
        selectedCategoryName = intent.getStringExtra("categoryName");
        btnFillWithAi.setVisibility(View.GONE);
    }

    private void handleNewNoteMode(Intent intent) {
        btnSave.setText("Simpan Catatan");

        String titleFromInspiration = intent.getStringExtra("note_title");
        String contentFromInspiration = intent.getStringExtra("template_text");
        autoCategoryNameFromInspiration = intent.getStringExtra("main_topic_for_category");

        if (titleFromInspiration != null) {
            etTitle.setText(titleFromInspiration);
        }

        if (contentFromInspiration != null) {
            etContent.setText(contentFromInspiration);
            btnFillWithAi.setVisibility(View.GONE);
        } else {
            btnFillWithAi.setVisibility(View.VISIBLE);
        }
    }


    private void saveOrUpdateNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Judul harus diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        int finalCategoryId = selectedCategoryId;

        // Logika untuk auto-kategori dari InspirationFragment tidak berubah,
        // tapi kita perlu dbHelper sementara untuk ini.
        if (!isEditMode && autoCategoryNameFromInspiration != null && selectedCategoryId == -1) {
            DBHelper categoryDbHelper = new DBHelper(this);
            int existingCatId = categoryDbHelper.getCategoryIdByName(autoCategoryNameFromInspiration);
            if (existingCatId != -1) {
                finalCategoryId = existingCatId;
            } else {
                finalCategoryId = (int) categoryDbHelper.addCategoryAndGetId(autoCategoryNameFromInspiration);
            }
        }

        // Buat objek Note yang akan dikirim
        Note note = new Note();
        note.setId(noteId); // Akan -1 jika note baru
        note.setTitle(title);
        note.setContent(content);
        note.setCategoryId(finalCategoryId);

        if (isEditMode) {
            // --- LOGIKA UPDATE DENGAN REPOSITORY ---
            noteRepository.updateNote(noteId, note, new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddNoteActivity.this, "Catatan diperbarui", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddNoteActivity.this, "Gagal memperbarui: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(AddNoteActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // --- LOGIKA SIMPAN DENGAN REPOSITORY ---
            noteRepository.addNote(note, new Callback<Note>() {
                @Override
                public void onResponse(@NonNull Call<Note> call, @NonNull Response<Note> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null && autoCategoryNameFromInspiration != null) {
                            Toast.makeText(AddNoteActivity.this, "Catatan disimpan di kategori '" + autoCategoryNameFromInspiration + "'", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(AddNoteActivity.this, "Catatan disimpan", Toast.LENGTH_SHORT).show();
                        }
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddNoteActivity.this, "Gagal menyimpan: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Note> call, @NonNull Throwable t) {
                    Toast.makeText(AddNoteActivity.this, "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // (Sisa kode untuk AI, loading dialog, dll, tidak ada perubahan)
    private void generateContentWithAI(String title) { showLoadingDialog(); String promptText = "Kamu adalah seorang guru yang ahli. Jelaskan secara ringkas dan jelas tentang topik: \"" + title + "\". " + "Berikan penjelasan dalam bentuk poin-poin atau paragraf singkat yang mudah dipahami oleh pemula. " + "Fokus pada konsep-konsep kunci, definisi, dan contoh sederhana jika memungkinkan. " + "KEMBALIKAN HANYA TEKS PENJELASANNYA, tanpa kalimat pembuka atau penutup seperti 'Tentu, ini penjelasannya'."; ArrayList<Message> messages = new ArrayList<>(); messages.add(new Message("user", promptText)); OpenAIRequest request = new OpenAIRequest("openai/gpt-3.5-turbo", messages, 0.7); openAIApi.createChatCompletion(request).enqueue(new Callback<OpenAIResponse>() { @Override public void onResponse(@NonNull Call<OpenAIResponse> call, @NonNull Response<OpenAIResponse> response) { hideLoadingDialog(); if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) { String generatedContent = response.body().choices.get(0).message.getContent(); etContent.setText(generatedContent); btnFillWithAi.setVisibility(View.GONE); } else { handleApiError(response); } } @Override public void onFailure(@NonNull Call<OpenAIResponse> call, @NonNull Throwable t) { hideLoadingDialog(); Toast.makeText(AddNoteActivity.this, "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show(); } }); }
    private void setupOpenAI() { OkHttpClient.Builder httpClient = new OkHttpClient.Builder(); httpClient.addInterceptor(chain -> { okhttp3.Request original = chain.request(); okhttp3.Request.Builder requestBuilder = original.newBuilder() .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY) .header("HTTP-Referer", getPackageName()) .header("X-Title", getString(R.string.app_name)); okhttp3.Request request = requestBuilder.build(); return chain.proceed(request); }); Retrofit retrofit = new Retrofit.Builder() .baseUrl("https://openrouter.ai/api/v1/") .addConverterFactory(GsonConverterFactory.create()) .client(httpClient.build()) .build(); openAIApi = retrofit.create(OpenAIApi.class); }
    private void showLoadingDialog() { if (loadingDialog != null && loadingDialog.isShowing()) return; AlertDialog.Builder builder = new AlertDialog.Builder(this); builder.setView(getLayoutInflater().inflate(R.layout.dialog_loading, null)); builder.setCancelable(false); loadingDialog = builder.create(); loadingDialog.show(); }
    private void hideLoadingDialog() { if (loadingDialog != null && loadingDialog.isShowing()) { loadingDialog.dismiss(); } }
    private void handleApiError(Response<OpenAIResponse> response) { String errorBody = "Tidak ada detail kesalahan."; try { if (response.errorBody() != null) { errorBody = response.errorBody().string(); } } catch (IOException e) { e.printStackTrace(); } new AlertDialog.Builder(this) .setTitle("Gagal Mendapatkan Respons") .setMessage("Kode Error: " + response.code() + "\n\nPesan:\n" + errorBody) .setPositiveButton("OK", null) .show(); }
}

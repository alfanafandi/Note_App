package com.example.note;

import android.graphics.Color; // <-- Import yang dibutuhkan logika lama
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.note.network.Message;
import com.example.note.network.OpenAIApi;
import com.example.note.network.OpenAIRequest;
import com.example.note.network.OpenAIResponse;
import com.example.note.network.QuizQuestion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizFragment extends Fragment {

    private static final String TAG = "QuizFragment";
    private static final String SPINNER_PLACEHOLDER = "Pilih Catatan untuk Kuis...";

    private LinearLayout llSetup;
    private Spinner spinnerNotes;
    private Button btnGenerateAiQuiz;
    private TextView tvQuizPlaceholder;
    private FrameLayout quizContainer;

    private TextView tvQuestionCounter, tvScore, tvQuestion;
    private RadioGroup radioGroupOptions;
    private RadioButton[] radioButtons = new RadioButton[4];
    private Button btnConfirmNext;
    private View quizView;

    private NoteRepository noteRepository;
    private Note currentNote = null;
    private List<Note> filteredNotes = new ArrayList<>();
    private List<QuizQuestion> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answerChecked = false;

    private AlertDialog loadingDialog;
    private OpenAIApi openAIApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        llSetup = view.findViewById(R.id.llSetup);
        spinnerNotes = view.findViewById(R.id.spinnerNotes);
        btnGenerateAiQuiz = view.findViewById(R.id.btnGenerateAiQuiz);
        tvQuizPlaceholder = view.findViewById(R.id.tvQuizPlaceholder);
        quizContainer = view.findViewById(R.id.quizContainer);

        noteRepository = new NoteRepository(requireContext());
        setupOpenAI();
        return view;
    }

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
            llSetup.setVisibility(View.GONE);
            generateQuizWithAI(contentToProcess);
        } else {
            llSetup.setVisibility(View.VISIBLE);
            quizContainer.setVisibility(View.GONE);
            loadNotesToSpinner();
            btnGenerateAiQuiz.setOnClickListener(v -> {
                if (currentNote != null) {
                    String content = currentNote.getContent();
                    if (content != null && !content.trim().isEmpty() && content.length() > 50) {
                        generateQuizWithAI(content);
                    } else {
                        Toast.makeText(requireContext(), "Catatan terlalu pendek untuk dibuat kuis.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Pilih catatan yang valid dari daftar.", Toast.LENGTH_SHORT).show();
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
                        tvQuizPlaceholder.setText("Tidak ada catatan yang bisa dijadikan kuis.\n(Minimal 50 karakter)");
                        spinnerNotes.setVisibility(View.GONE);
                        btnGenerateAiQuiz.setVisibility(View.GONE);
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
                            } else {
                                currentNote = filteredNotes.get(position - 1);
                                Toast.makeText(getContext(), "Catatan '" + currentNote.getTitle() + "' dipilih.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            currentNote = null;
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Gagal memuat catatan dari server.", Toast.LENGTH_SHORT).show();
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

    private void generateQuizWithAI(String textContent) {
        showLoadingDialog();
        String promptText = "Berdasarkan teks berikut, buat 5 soal pilihan ganda dalam format JSON. " +
                "SETIAP objek dalam array HARUS memiliki tiga kunci: 'question' (String), 'options' (Array of 4 Strings), dan 'correctAnswer' (String). " +
                "Nilai dari 'correctAnswer' HARUS sama persis dengan salah satu string di dalam array 'options'. " +
                "KEMBALIKAN HANYA SEBUAH ARRAY JSON, tanpa teks atau markdown 'json'.\n\nTeks Asli:\n" + textContent;

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message("user", promptText));
        OpenAIRequest request = new OpenAIRequest("openai/gpt-3.5-turbo", messages, 0.5);

        openAIApi.createChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenAIResponse> call, @NonNull Response<OpenAIResponse> response) {
                hideLoadingDialog();
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                        String jsonResponse = response.body().choices.get(0).message.getContent();
                        processJsonResponse(jsonResponse);
                    } else {
                        handleApiError(response);
                        resetToInitialState();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<OpenAIResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    resetToInitialState();
                }
            }
        });
    }

    private void processJsonResponse(String jsonResponse) {
        if (!isAdded()) return;
        try {
            List<QuizQuestion> parsedList = new Gson().fromJson(jsonResponse, new TypeToken<List<QuizQuestion>>() {}.getType());
            if (parsedList == null || parsedList.isEmpty()) {
                throw new Exception("AI tidak menghasilkan daftar pertanyaan yang valid.");
            }
            List<QuizQuestion> validQuestions = new ArrayList<>();
            for (QuizQuestion q : parsedList) {
                if (q.question != null && q.correctAnswer != null && q.options != null && q.options.size() > 1) {
                    validQuestions.add(q);
                } else {
                    Log.w(TAG, "Satu pertanyaan dari AI tidak valid dan dilewati.");
                }
            }
            if (validQuestions.isEmpty()) {
                throw new Exception("Tidak ada satupun pertanyaan yang valid dari AI.");
            }
            startQuiz(validQuestions);
        } catch (Exception e) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Gagal Memproses Kuis")
                    .setMessage("AI memberikan respons yang tidak terduga.\n\n" + e.getMessage() + "\n\nJSON Mentah:\n" + jsonResponse)
                    .setPositiveButton("OK", (dialog, which) -> resetToInitialState())
                    .show();
        }
    }

    private void startQuiz(List<QuizQuestion> questions) {
        if (!isAdded()) return;
        questionList.clear();
        questionList.addAll(questions);
        quizContainer.removeAllViews();
        quizView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_quiz_view, quizContainer, true);
        tvQuestionCounter = quizView.findViewById(R.id.tvQuestionCounter);
        tvScore = quizView.findViewById(R.id.tvScore);
        tvQuestion = quizView.findViewById(R.id.tvQuestion);
        radioGroupOptions = quizView.findViewById(R.id.radioGroupOptions);
        radioButtons[0] = quizView.findViewById(R.id.option1);
        radioButtons[1] = quizView.findViewById(R.id.option2);
        radioButtons[2] = quizView.findViewById(R.id.option3);
        radioButtons[3] = quizView.findViewById(R.id.option4);
        btnConfirmNext = quizView.findViewById(R.id.btnConfirmNext);
        currentQuestionIndex = 0;
        score = 0;
        answerChecked = false;
        llSetup.setVisibility(View.GONE);
        quizContainer.setVisibility(View.VISIBLE);
        showQuestion();
        btnConfirmNext.setOnClickListener(v -> {
            if (!answerChecked) {
                checkAnswer();
            } else {
                showNextQuestion();
            }
        });
    }

    private void showQuestion() {
        if (!isAdded() || questionList.isEmpty()) return;
        answerChecked = false;
        radioGroupOptions.clearCheck();
        // 🔥 KEMBALIKAN KE LOGIKA LAMA: Reset warna background dan teks ke normal
        for (RadioButton button : radioButtons) {
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(Color.BLACK);
            button.setEnabled(true);
        }

        QuizQuestion question = questionList.get(currentQuestionIndex);
        tvQuestion.setText(question.question);
        tvQuestionCounter.setText("Soal: " + (currentQuestionIndex + 1) + "/" + questionList.size());
        tvScore.setText("Skor: " + score);

        List<String> options = new ArrayList<>(question.options);
        Collections.shuffle(options);
        for (int i = 0; i < radioButtons.length; i++) {
            if (i < options.size()) {
                radioButtons[i].setText(options.get(i));
                radioButtons[i].setVisibility(View.VISIBLE);
            } else {
                radioButtons[i].setVisibility(View.GONE);
            }
        }
        btnConfirmNext.setText("Konfirmasi Jawaban");
    }

    private void checkAnswer() {
        if (!isAdded()) return;
        answerChecked = true;
        RadioButton selectedButton = quizView.findViewById(radioGroupOptions.getCheckedRadioButtonId());
        if (selectedButton == null) {
            Toast.makeText(requireContext(), "Pilih jawaban terlebih dahulu!", Toast.LENGTH_SHORT).show();
            answerChecked = false;
            return;
        }

        String correctAnswer = questionList.get(currentQuestionIndex).correctAnswer;
        // 🔥 KEMBALIKAN KE LOGIKA LAMA: Nonaktifkan semua tombol
        for (RadioButton button : radioButtons) {
            button.setEnabled(false);
        }

        // 🔥 KEMBALIKAN KE LOGIKA LAMA: Cek jawaban dan warnai
        if (selectedButton.getText().toString().equals(correctAnswer)) {
            selectedButton.setBackgroundColor(Color.GREEN);
            score += (100 / questionList.size());
        } else {
            selectedButton.setBackgroundColor(Color.RED);
            // Tunjukkan jawaban yang benar
            for (RadioButton button : radioButtons) {
                if (button.getText().toString().equals(correctAnswer)) {
                    button.setBackgroundColor(Color.GREEN);
                    break;
                }
            }
        }

        tvScore.setText("Skor: " + score);
        if (currentQuestionIndex < questionList.size() - 1) {
            btnConfirmNext.setText("Soal Berikutnya");
        } else {
            btnConfirmNext.setText("Selesai");
        }
    }

    private void showNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Kuis Selesai!")
                .setMessage("Skor akhir Anda: " + score)
                .setPositiveButton("Ulangi Kuis", (dialog, which) -> {
                    resetToInitialState();
                    llSetup.setVisibility(View.GONE);
                    generateQuizWithAI(currentNote.getContent());
                })
                .setNegativeButton("Kembali", (dialog, which) -> resetToInitialState())
                .setCancelable(false)
                .show();
    }

    private void resetToInitialState() {
        if (!isAdded()) return;
        llSetup.setVisibility(View.VISIBLE);
        quizContainer.setVisibility(View.GONE);
        questionList.clear();
        currentQuestionIndex = 0;
        score = 0;
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
        if (!isAdded() || (loadingDialog != null && loadingDialog.isShowing())) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null));
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
        if (!isAdded()) return;
        String errorBody = "Gagal memuat detail kesalahan.";
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

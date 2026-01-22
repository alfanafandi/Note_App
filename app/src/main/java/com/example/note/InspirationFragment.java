package com.example.note;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

public class InspirationFragment extends Fragment {

    private LinearLayout llInitialState;
    private Button btnMainAction;
    private LinearLayout planContainer;
    private RelativeLayout headerPlan;
    private TextView tvPlanTitle;
    private ImageButton btnDeletePlan;
    private ImageButton btnSwitchPlan;
    private LinearLayout contentPlanContainer;
    private DBHelper dbHelper;
    private AlertDialog loadingDialog;
    private OpenAIApi openAIApi;

    public InspirationFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inspiration, container, false);

        llInitialState = view.findViewById(R.id.llInitialState);
        btnMainAction = view.findViewById(R.id.btnMainAction);
        planContainer = view.findViewById(R.id.planContainer);
        headerPlan = view.findViewById(R.id.headerPlan);
        tvPlanTitle = view.findViewById(R.id.tvPlanTitle);
        btnDeletePlan = view.findViewById(R.id.btnDeletePlan);
        btnSwitchPlan = view.findViewById(R.id.btnSwitchPlan);
        contentPlanContainer = view.findViewById(R.id.contentPlanContainer);

        dbHelper = new DBHelper(getContext());
        setupOpenAI();
        loadActiveMindMap();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActiveMindMap();
    }

    private void loadActiveMindMap() {
        MindMap activeMindMap = dbHelper.getActiveMindMap();
        if (activeMindMap != null && activeMindMap.getSubTopics() != null && !activeMindMap.getSubTopics().isEmpty()) {
            displayPlanFromData(activeMindMap);
        } else {
            showInitialState();
        }
    }

    private void showInitialState() {
        planContainer.setVisibility(View.GONE);
        llInitialState.setVisibility(View.VISIBLE);
        btnMainAction.setText("✨ Buat Rencana Belajar");
        btnMainAction.setOnClickListener(v -> showTopicInputDialog());
    }

    private void displayPlanFromData(MindMap mindMap) {
        llInitialState.setVisibility(View.GONE);
        planContainer.setVisibility(View.VISIBLE);
        contentPlanContainer.removeAllViews();
        tvPlanTitle.setText(mindMap.getTopicTitle());

        btnDeletePlan.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Rencana Belajar")
                    .setMessage("Apakah Anda yakin ingin menghapus rencana belajar ini secara permanen?")
                    .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                        dbHelper.deleteMindMap(mindMap.getId());
                        showInitialState();
                        Toast.makeText(getContext(), "Rencana belajar dihapus.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnSwitchPlan.setOnClickListener(v -> showHistoryDialog());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        List<MindMap.SubTopic> subTopics = mindMap.getSubTopics();
        if (subTopics.isEmpty() || getContext() == null) return;

        for (MindMap.SubTopic subTopic : subTopics) {
            View branchView = createBranchView(inflater, subTopic, mindMap.getTopicTitle());
            contentPlanContainer.addView(branchView);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) branchView.getLayoutParams();
            params.setMargins(0, 0, 0, 24);
            branchView.setLayoutParams(params);
        }
    }

    private View createBranchView(LayoutInflater inflater, MindMap.SubTopic subTopic, String mainTopicTitle) {
        View branchView = inflater.inflate(R.layout.item_mind_map_branch, contentPlanContainer, false);

        RelativeLayout headerBranch = branchView.findViewById(R.id.headerBranch);
        TextView tvBranchTitle = branchView.findViewById(R.id.tvBranchTitle);
        CheckBox cbBranch = branchView.findViewById(R.id.cbBranch);
        ImageView ivExpandIcon = branchView.findViewById(R.id.ivExpandIcon);
        LinearLayout contentBranch = branchView.findViewById(R.id.contentBranch);
        Button btnExplainWithAi = branchView.findViewById(R.id.btnExplainWithAi);
        TextView tvGeneratedContent = branchView.findViewById(R.id.tvGeneratedContent);
        LinearLayout llActionButtons = branchView.findViewById(R.id.llActionButtons);
        Button btnSaveToNote = branchView.findViewById(R.id.btnSaveToNote);
        Button btnGoToFlashcard = branchView.findViewById(R.id.btnGoToFlashcard);
        Button btnGoToQuiz = branchView.findViewById(R.id.btnGoToQuiz);

        tvBranchTitle.setText(subTopic.getTitle());
        cbBranch.setChecked(subTopic.isCompleted());
        updateTextStyle(tvBranchTitle, subTopic.isCompleted());

        String existingContent = subTopic.getContent();
        if (existingContent != null && !existingContent.isEmpty()) {
            tvGeneratedContent.setText(existingContent);
            btnExplainWithAi.setVisibility(View.GONE);
            tvGeneratedContent.setVisibility(View.VISIBLE);
            llActionButtons.setVisibility(View.VISIBLE);
        } else {
            btnExplainWithAi.setVisibility(View.VISIBLE);
            tvGeneratedContent.setVisibility(View.GONE);
            llActionButtons.setVisibility(View.GONE);
        }

        headerBranch.setOnClickListener(v -> {
            boolean isVisible = contentBranch.getVisibility() == View.VISIBLE;
            contentBranch.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            float targetRotation = isVisible ? 0f : 180f;
            ivExpandIcon.animate().rotation(targetRotation).setDuration(200).start();
        });

        btnExplainWithAi.setOnClickListener(v -> generateSubTopicContentWithAI(subTopic, tvGeneratedContent, btnExplainWithAi, llActionButtons, mainTopicTitle));

        cbBranch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updateSubTopicStatus(subTopic.getId(), isChecked);
            updateTextStyle(tvBranchTitle, isChecked);
        });

        btnGoToFlashcard.setOnClickListener(v -> navigateToTab(R.id.navigation_flashcard, subTopic.getId()));
        btnGoToQuiz.setOnClickListener(v -> navigateToTab(R.id.navigation_quiz, subTopic.getId()));

        btnSaveToNote.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddNoteActivity.class);
            intent.putExtra("note_title", subTopic.getTitle());
            intent.putExtra("template_text", subTopic.getContent());
            intent.putExtra("main_topic_for_category", mainTopicTitle);
            startActivity(intent);
        });

        return branchView;
    }

    private void navigateToTab(int destinationTabId, int subTopicId) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        String subTopicContent = dbHelper.getSubTopicContent(subTopicId);
        String contentForTest = (subTopicContent != null && !subTopicContent.isEmpty())
                ? subTopicContent
                : dbHelper.getSubTopicTitle(subTopicId);

        if (contentForTest == null || contentForTest.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada konten untuk diuji.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("NOTE_CONTENT_FROM_DETAIL", contentForTest);
        mainActivity.navigateToFragment(destinationTabId, bundle);
    }

    private void generateSubTopicContentWithAI(MindMap.SubTopic subTopic, TextView targetTextView, Button buttonToHide, LinearLayout actionsToShow, String mainTopicTitle) {
        showLoadingDialog();

        String prompt = "Anda adalah seorang guru yang ahli. Dalam konteks rencana belajar tentang \"" + mainTopicTitle + "\", " +
                "jelaskan secara ringkas dan jelas mengenai sub-topik: \"" + subTopic.getTitle() + "\". " +
                "Berikan penjelasan dalam bentuk poin-poin atau paragraf singkat yang mudah dipahami oleh pemula. " +
                "Fokus pada konsep-konsep kunci, definisi, dan contoh sederhana jika memungkinkan. " +
                "KEMBALIKAN HANYA TEKS PENJELASANNYA, tanpa kalimat pembuka atau penutup seperti 'Tentu, ini penjelasannya'.";

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));
        OpenAIRequest request = new OpenAIRequest("openai/gpt-3.5-turbo", messages, 0.7);

        openAIApi.createChatCompletion(request).enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenAIResponse> call, @NonNull Response<OpenAIResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String generatedContent = response.body().choices.get(0).message.getContent().trim();
                    dbHelper.updateSubTopicContent(subTopic.getId(), generatedContent);
                    targetTextView.setText(generatedContent);
                    subTopic.setContent(generatedContent);

                    buttonToHide.setVisibility(View.GONE);
                    targetTextView.setVisibility(View.VISIBLE);
                    actionsToShow.setVisibility(View.VISIBLE);

                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(getContext(), "Gagal generate konten: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Gagal membaca respons error.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenAIResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                Toast.makeText(getContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void generateMindMapWithAI(String topic) {
        showLoadingDialog();

        String promptText = "Anda adalah seorang perancang kurikulum yang handal. " +
                "Buatlah rencana belajar langkah demi langkah untuk topik utama: \"" + topic + "\". " +
                "Rencana ini ditujukan untuk seorang **pemula**. Buat antara 5 hingga 7 sub-topik yang logis dan berurutan. " +
                "Mulai dari konsep paling dasar hingga ke yang lebih spesifik. " +
                "KEMBALIKAN HANYA SEBUAH ARRAY JSON dari string, tanpa teks tambahan, tanpa markdown 'json', dan tanpa komentar. " +
                "Contoh format balasan yang benar: [\"Pengenalan " + topic + "\", \"Konsep Dasar\", \"Studi Kasus Sederhana\"]";

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message("user", promptText));
        OpenAIRequest request = new OpenAIRequest("openai/gpt-3.5-turbo", messages, 0.5);

        Call<OpenAIResponse> call = openAIApi.createChatCompletion(request);
        call.enqueue(new Callback<OpenAIResponse>() {
            @Override
            public void onResponse(@NonNull Call<OpenAIResponse> call, @NonNull Response<OpenAIResponse> response) {
                hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String jsonResponse = response.body().choices.get(0).message.getContent();
                    Log.d("AI_RESPONSE", "Respons JSON mentah: " + jsonResponse);
                    processMindMapResponse(topic, jsonResponse);
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OpenAIResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showTopicInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Topik Belajar Baru");
        builder.setMessage("Masukkan topik utama yang ingin Anda pelajari:");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(48, 16, 48, 16);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Buat Rencana", (dialog, which) -> {
            String topic = input.getText().toString().trim();
            if (!topic.isEmpty()) {
                generateMindMapWithAI(topic);
            } else {
                Toast.makeText(getContext(), "Topik tidak boleh kosong.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void processMindMapResponse(String mainTopicTitle, String jsonResponse) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            List<String> subTopicTitles = gson.fromJson(jsonResponse, listType);

            if (subTopicTitles == null || subTopicTitles.isEmpty()) {
                throw new Exception("AI tidak memberikan daftar sub-topik.");
            }

            long mindMapId = dbHelper.createMindMap(mainTopicTitle, subTopicTitles);
            dbHelper.setActiveMindMap((int) mindMapId);
            loadActiveMindMap();

        } catch (Exception e) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Gagal Memproses Rencana")
                    .setMessage("AI memberikan respons yang tidak terduga dan tidak dapat diproses.\n\nError: " + e.getMessage() + "\n\nRespons Mentah:\n" + jsonResponse)
                    .setPositiveButton("OK", null)
                    .show();
            Log.e("MIND_MAP_ERROR", "Gagal parse JSON: " + jsonResponse, e);
        }
    }

    private void showHistoryDialog() {
        List<MindMap> allMindMaps = dbHelper.getAllMindMaps();

        List<String> dialogOptions = new ArrayList<>();
        dialogOptions.add("Buat Rencana Baru...");

        for (MindMap map : allMindMaps) {
            dialogOptions.add(map.getTopicTitle());
        }

        final CharSequence[] items = dialogOptions.toArray(new CharSequence[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Pilih Rencana Belajar")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showTopicInputDialog();
                    } else {
                        MindMap selectedMindMap = allMindMaps.get(which - 1);
                        dbHelper.setActiveMindMap(selectedMindMap.getId());
                        loadActiveMindMap();
                        Toast.makeText(getContext(), "Beralih ke: " + selectedMindMap.getTopicTitle(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .create()
                .show();
    }

    private void updateTextStyle(TextView textView, boolean isCompleted) {
        if (isCompleted) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setAlpha(0.6f);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setAlpha(1.0f);
        }
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
        if (getContext() == null) return;
        if (loadingDialog != null && loadingDialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        if (getContext() == null) return;
        String errorBody = "Gagal memuat detail kesalahan.";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        new AlertDialog.Builder(getContext())
                .setTitle("Gagal Mendapatkan Respons")
                .setMessage("Kode Error: " + response.code() + "\n\nPesan:\n" + errorBody)
                .setPositiveButton("OK", null)
                .show();
    }
}

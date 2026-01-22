package com.example.note.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizQuestion {

    // @SerializedName memberitahu Gson untuk mencari nama-nama ini di JSON
    // dan memasukkannya ke variabel di bawahnya.

    @SerializedName(value="question", alternate={"soal", "pertanyaan"})
    public String question;

    @SerializedName(value="options", alternate={"choices", "pilihan"})
    public List<String> options;

    @SerializedName(value="correctAnswer", alternate={"answer", "jawaban_benar", "jawaban"})
    public String correctAnswer;
}

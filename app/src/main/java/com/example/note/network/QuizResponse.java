package com.example.note.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizResponse {

    @SerializedName("quiz")
    public List<QuizQuestion> quiz;

}
    
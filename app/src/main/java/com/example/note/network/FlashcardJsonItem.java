package com.example.note.network;

import com.google.gson.annotations.SerializedName;

public class FlashcardJsonItem {

    @SerializedName("question")
    public String question;

    @SerializedName("answer")
    public String answer;
}
    
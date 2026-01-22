package com.example.note.network;

// PASTIKAN IMPORT INI BENAR
import com.example.note.network.Message;
import java.util.ArrayList;

public class OpenAIRequest {
    private String model;
    private ArrayList<Message> messages; // Pastikan ini menggunakan 'Message' dari package network
    private double temperature;

    public OpenAIRequest(String model, ArrayList<Message> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }
}

package com.example.note.network;

public class Message {
    private String role;
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    // TAMBAHKAN METHOD INI
    public String getContent() {
        return content;
    }
}

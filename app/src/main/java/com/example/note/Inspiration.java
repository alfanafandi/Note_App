package com.example.note;

public class Inspiration {
    private String prompt;
    private String author;

    public Inspiration(String prompt, String author) {
        this.prompt = prompt;
        this.author = author;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getAuthor() {
        return author;
    }
}

package com.example.note;

import java.util.List;

public class MindMap {
    private int id;
    private String topicTitle;
    private boolean isActive;
    private List<SubTopic> subTopics;

    // Konstruktor, getter, dan setter untuk MindMap
    public MindMap(int id, String topicTitle, boolean isActive, List<SubTopic> subTopics) {
        this.id = id;
        this.topicTitle = topicTitle;
        this.isActive = isActive;
        this.subTopics = subTopics;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<SubTopic> getSubTopics() {
        return subTopics;
    }

    public void setSubTopics(List<SubTopic> subTopics) {
        this.subTopics = subTopics;
    }

    // Inner class untuk SubTopic
    public static class SubTopic {
        private int id;
        private String title;
        private String content; // Konten hasil generate AI
        private boolean isCompleted;

        public SubTopic(int id, String title, String content, boolean isCompleted) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.isCompleted = isCompleted;
        }

        // Getter
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public boolean isCompleted() { return isCompleted; }

        // Setter
        public void setId(int id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setCompleted(boolean completed) { isCompleted = completed; }

        // 🔥🔥 PERBAIKAN: METHOD YANG HILANG DITAMBAHKAN DI SINI 🔥🔥
        public void setContent(String content) {
            this.content = content;
        }
    }
}

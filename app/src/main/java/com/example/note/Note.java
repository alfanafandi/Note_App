package com.example.note;

import com.google.gson.annotations.SerializedName;

public class Note {
    private int id;
    private String title;
    private String content;

    // Anotasi agar Gson cocok dengan JSON dari Laravel ('created_at')
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("category_id")
    private Integer categoryId;

    // 'categoryName' tidak ada di JSON, jadi kita tidak perlu anotasi
    private String categoryName;

    public Note() {
        // Konstruktor kosong ini dibutuhkan oleh Gson/Retrofit untuk deserialisasi JSON
    }

    // Konstruktor lain yang sudah ada tidak perlu diubah
    public Note(int id, String title, String content, String createdAt, String updatedAt, Integer categoryId, String categoryName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Note(int id, String title, String content, String createdAt, String updatedAt) {
        this(id, title, content, createdAt, updatedAt, null, "Tanpa Kategori");
    }

    public Note(String title, String content, Integer categoryId) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
    }

    // ... sisa getter dan setter tidak berubah ...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getCategoryId() {
        return categoryId != null ? categoryId : -1; // hindari NullPointer
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : "Tanpa Kategori";
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}

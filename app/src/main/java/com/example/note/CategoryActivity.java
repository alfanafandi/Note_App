package com.example.note;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private TextInputEditText etCategoryName;
    private ImageButton btnAddCategory, btnAddCategoryConfirm, btnBackCategory;
    private TextView tvToolbarTitle;
    private LinearLayout layoutAddCategory; // 🔹 container input + tombol ✔
    private DBHelper dbHelper;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;

    // 🔹 mode = "main" (buka dari MainActivity) atau "addnote" (buka dari AddNoteActivity)
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // --- Inisialisasi View ---
        rvCategories = findViewById(R.id.rvCategories);
        etCategoryName = findViewById(R.id.etCategoryName);
        layoutAddCategory = findViewById(R.id.layoutAddCategory);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnAddCategoryConfirm = findViewById(R.id.btnAddCategoryConfirm);
        btnBackCategory = findViewById(R.id.btnBackCategory);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);

        dbHelper = new DBHelper(this);

        // 🔹 Cek mode (default = "main")
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "main";

        // --- Setup RecyclerView ---
        categoryList = dbHelper.getAllCategories();

        // 🔹 Adapter dengan click listener
        categoryAdapter = new CategoryAdapter(categoryList, this, category -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedCategoryId", category.getId());
            resultIntent.putExtra("selectedCategoryName", category.getName());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);

        // --- Tombol kembali ---
        btnBackCategory.setOnClickListener(v -> finish());

        // --- Mode awal: tampil judul dan tombol tambah ---
        setAddCategoryMode(false);

        // --- Klik tombol tambah ---
        btnAddCategory.setOnClickListener(v -> {
            setAddCategoryMode(true);
            etCategoryName.requestFocus();
        });

        // --- Klik tombol konfirmasi tambah ---
        btnAddCategoryConfirm.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();

            if (name.isEmpty()) {
                etCategoryName.setError("Nama kategori tidak boleh kosong");
                return;
            }

            boolean success = dbHelper.addCategory(name);
            if (success) {
                Toast.makeText(this, "Kategori \"" + name + "\" ditambahkan", Toast.LENGTH_SHORT).show();
                etCategoryName.setText("");
                refreshCategoryList();
            } else {
                Toast.makeText(this, "Kategori sudah ada atau gagal disimpan", Toast.LENGTH_SHORT).show();
            }

            setAddCategoryMode(false);
        });
    }

    // =======================================================
    // 🔹 Ubah tampilan toolbar antara mode normal dan tambah
    // =======================================================
    private void setAddCategoryMode(boolean isAdding) {
        if (isAdding) {
            tvToolbarTitle.setVisibility(View.GONE);
            layoutAddCategory.setVisibility(View.VISIBLE);
            btnAddCategory.setVisibility(View.GONE);
        } else {
            tvToolbarTitle.setVisibility(View.VISIBLE);
            layoutAddCategory.setVisibility(View.GONE);
            btnAddCategory.setVisibility(View.VISIBLE);
        }
    }

    // =======================================================
    // 🔹 Refresh daftar kategori setelah tambah
    // =======================================================
    private void refreshCategoryList() {
        categoryList.clear();
        categoryList.addAll(dbHelper.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }
}

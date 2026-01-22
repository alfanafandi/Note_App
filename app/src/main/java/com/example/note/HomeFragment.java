package com.example.note;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Import untuk Retrofit Callback
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvNotes;
    private TabLayout tabLayoutCategories;
    private TextView tvEmpty;

    // private DBHelper dbHelper; // <-- HAPUS INI
    private NoteRepository noteRepository; // <-- GANTI DENGAN INI

    private NoteAdapter noteAdapter;
    // Daftar untuk menyimpan SEMUA catatan dari server/db, sebelum difilter
    private List<Note> allNotesFromServer = new ArrayList<>();
    // Daftar untuk catatan yang sedang ditampilkan di RecyclerView
    private List<Note> currentlyDisplayedNotes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Inisialisasi View ---
        rvNotes = view.findViewById(R.id.rvNotes);
        tabLayoutCategories = view.findViewById(R.id.tabLayoutCategories);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        // --- GANTI DBHelper DENGAN NoteRepository ---
        // dbHelper = new DBHelper(getContext()); // HAPUS INI
        noteRepository = new NoteRepository(requireContext()); // GANTI DENGAN INI

        // --- Setup RecyclerView ---
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        // Adapter diinisialisasi dengan daftar kosong, akan diisi nanti
        noteAdapter = new NoteAdapter(currentlyDisplayedNotes, requireContext());
        rvNotes.setAdapter(noteAdapter);

        // Listener untuk klik item di RecyclerView
        noteAdapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(getActivity(), DetailNoteActivity.class);
            // Kirim semua data yang dibutuhkan oleh DetailNoteActivity
            intent.putExtra("note_id", note.getId());
            intent.putExtra("title", note.getTitle());
            intent.putExtra("content", note.getContent());
            intent.putExtra("categoryId", note.getCategoryId());
            intent.putExtra("categoryName", note.getCategoryName());
            startActivity(intent);
        });

        return view;
    }

    // Fungsi untuk search, dipanggil dari MainActivity
    public void performSearch(String query) {
        if (noteAdapter != null) {
            noteAdapter.getFilter().filter(query);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Setiap kali fragment ditampilkan, muat ulang data dari awal
        loadInitialNotes();
    }

    // Fungsi utama yang baru untuk mengambil data dari NoteRepository
    private void loadInitialNotes() {
        tvEmpty.setText("Memuat catatan...");
        tvEmpty.setVisibility(View.VISIBLE);
        rvNotes.setVisibility(View.GONE);

        noteRepository.getAllNotes(new Callback<List<Note>>() {
            @Override
            public void onResponse(@NonNull Call<List<Note>> call, @NonNull Response<List<Note>> response) {
                // Pastikan fragment masih ada sebelum memanipulasi view
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    allNotesFromServer.clear();
                    allNotesFromServer.addAll(response.body());
                    // Setelah data berhasil dimuat, baru setup tab kategori
                    setupTabs();
                } else {
                    tvEmpty.setText("Gagal memuat catatan.");
                    Toast.makeText(getContext(), "Gagal memuat data: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Note>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                tvEmpty.setText("Gagal terhubung ke server.\nPastikan server (artisan serve) berjalan.");
                Toast.makeText(getContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Fungsi untuk mengatur tab, sekarang dipanggil setelah data berhasil dimuat
    private void setupTabs() {
        // Simpan posisi tab yang sedang aktif
        int currentPosition = tabLayoutCategories.getSelectedTabPosition();
        if (currentPosition == -1) currentPosition = 0; // Default ke tab 'All'

        tabLayoutCategories.clearOnTabSelectedListeners();
        tabLayoutCategories.removeAllTabs();

        tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText("All"));

        // Untuk daftar kategori, kita masih bisa pakai DBHelper karena tidak terkait langsung dengan data catatan
        DBHelper categoryDbHelper = new DBHelper(requireContext());
        List<Category> categories = categoryDbHelper.getAllCategories();
        for (Category cat : categories) {
            tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText(cat.getName()));
        }
        tabLayoutCategories.addTab(tabLayoutCategories.newTab().setText("Tanpa Kategori"));

        // Kembalikan pilihan ke tab yang sebelumnya dipilih
        if (currentPosition < tabLayoutCategories.getTabCount()) {
            tabLayoutCategories.selectTab(tabLayoutCategories.getTabAt(currentPosition));
            // Langsung filter catatan berdasarkan tab yang dipilih
            filterNotesByTab(tabLayoutCategories.getTabAt(currentPosition));
        }

        tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterNotesByTab(tab);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // Fungsi untuk memfilter daftar 'allNotesFromServer' berdasarkan tab yang dipilih
    private void filterNotesByTab(TabLayout.Tab tab) {
        if (tab == null) return;
        String name = tab.getText().toString();
        List<Note> filteredList = new ArrayList<>();

        if (name.equals("All")) {
            filteredList.addAll(allNotesFromServer);
        } else if (name.equals("Tanpa Kategori")) {
            for(Note note : allNotesFromServer) {
                if(note.getCategoryId() == 0 || note.getCategoryId() == -1) {
                    filteredList.add(note);
                }
            }
        } else {
            // Dapatkan ID kategori dari SQLite (ini tidak apa-apa, karena hanya untuk filter)
            DBHelper categoryDbHelper = new DBHelper(requireContext());
            int catId = categoryDbHelper.getCategoryIdByName(name);
            for(Note note : allNotesFromServer) {
                if(note.getCategoryId() == catId) {
                    filteredList.add(note);
                }
            }
        }

        // Perbarui daftar yang akan ditampilkan oleh adapter
        currentlyDisplayedNotes.clear();
        currentlyDisplayedNotes.addAll(filteredList);
        noteAdapter.updateData(currentlyDisplayedNotes);

        // Atur visibilitas teks "kosong" atau RecyclerView
        if (currentlyDisplayedNotes.isEmpty()) {
            rvNotes.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Tidak ada catatan di kategori ini.");
        } else {
            rvNotes.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}

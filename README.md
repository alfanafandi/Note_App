# Panduan Penggunaan Aplikasi Note (AI-Powered)

Selamat datang di panduan penggunaan aplikasi **Note**. Aplikasi ini bukan sekadar pencatat biasa, melainkan asisten belajar cerdas yang terintegrasi dengan kecerdasan buatan (AI) untuk membantu Anda membuat ringkasan, kuis, flashcard, hingga rencana belajar yang terstruktur.

## Daftar Isi

1. [Pengenalan Aplikasi](#1-pengenalan-aplikasi)
2. [Persiapan Awal](#2-persiapan-awal)
3. [Fitur Utama & Cara Penggunaan](#3-fitur-utama--cara-penggunaan)
   - [Manajemen Catatan (Notes)](#a-manajemen-catatan-notes)
   - [Rencana Belajar AI (Inspiration)](#b-rencana-belajar-ai-inspiration)
   - [Kuis Otomatis (Quiz)](#c-kuis-otomatis-quiz)
   - [Flashcard AI](#d-flashcard-ai)
4. [Catatan Teknis](#4-catatan-teknis)

---

## 1. Pengenalan Aplikasi

Aplikasi ini dirancang untuk pelajar, mahasiswa, atau siapa saja yang ingin belajar lebih efektif. Fitur utamanya meliputi:

- **Pencatatan**: Tulis dan simpan materi pelajaran.
- **Kategorisasi**: Kelompokkan catatan agar rapi.
- **AI Learning Plan**: Minta AI membuatkan kurikulum belajar untuk topik apa pun.
- **AI Quiz & Flashcard**: Ubah catatan Anda menjadi soal latihan secara otomatis.

---

## 2. Persiapan Awal

Sebelum menggunakan fitur berbasis AI (Rencana Belajar, Kuis, Flashcard), pastikan perangkat Anda terhubung ke **Internet**.

> **Catatan untuk Pengembang/Builder:**
> Aplikasi ini memerlukan **OpenAI API Key** agar fitur AI berfungsi. Kunci ini harus dikonfigurasi dalam file `local.properties` project Android sebelum aplikasi dibangun (Build).
>
> ```properties
> OPENAI_API_KEY=sk-proj-xxxx...
> ```

---

## 3. Fitur Utama & Cara Penggunaan

### A. Manajemen Catatan (Notes)

Halaman utama (**Home**) menampilkan daftar catatan Anda.

**1. Membuat Catatan Baru**

- Ketuk tombol **+** (Floating Action Button) di pojok kanan bawah.
- Masukkan **Judul** dan **Isi Catatan**.
- (Opsional) Pilih **Kategori** untuk mengelompokkan catatan.
- Tekan tombol **Simpan** (ikon disket) di pojok kanan atas.

**2. Mengelompokkan dengan Kategori**

- Di halaman utama, terdapat tab kategori (misal: _All, Personal, Work_).
- Untuk membuat kategori baru:
  - Ketuk menu titik tiga di pojok kanan atas -> **Kategori**.
  - Tambahkan nama kategori baru.
- Saat membuat catatan, pilih kategori yang sesuai agar catatan muncul di tab tersebut.

**3. Mencari Catatan**

- Gunakan ikon **Pencarian** (kaca pembesar) di menu atas untuk mencari catatan berdasarkan judul atau isi.

---

### B. Rencana Belajar AI (Inspiration)

Fitur ini (ikon lampu di navigasi bawah) adalah "otak" dari aplikasi ini. Anda bisa meminta AI membuatkan peta jalan (roadmap) belajar.

**1. Membuat Rencana Baru**

- Masuk ke tab **Inspiration**.
- Jika belum ada rencana, ketuk tombol **✨ Buat Rencana Belajar**.
- Masukkan topik yang ingin dipelajari (contoh: _"Belajar Bahasa Jepang Pemula"_ atau _"Sejarah Perang Dunia II"_).
- Ketuk **Buat Rencana**. AI akan berpikir sejenak dan menyusun daftar sub-topik (Materi) yang harus Anda pelajari secara berurutan.

**2. Mengelola Rencana**

- **Daftar Materi**: Rencana akan muncul sebagai daftar _checklist_.
- **Tandai Selesai**: Centang kotak (checklist) jika Anda sudah memahami sub-topik tersebut.
- **Hapus/Ganti Rencana**: Gunakan ikon tempat sampah untuk menghapus, atau ikon panah putar untuk melihat riwayat rencana belajar sebelumnya.

**3. Penjelasan Otomatis (Explain with AI)**

- Klik judul sub-topik untuk membuka detailnya.
- Jika materi masih kosong, ketuk tombol **Jelaskan dengan AI**.
- AI akan menuliskan rangkuman materi lengkap untuk sub-topik tersebut secara otomatis.

**4. Aksi Lanjutan**
Setelah materi dibuat oleh AI, Anda bisa:

- **Simpan ke Catatan**: Menyimpan materi tersebut sebagai catatan biasa agar bisa diedit.
- **Ingin Kuis/Flashcard?**: Langsung lompat ke menu Kuis atau Flashcard menggunakan materi tersebut.

---

### C. Kuis Otomatis (Quiz)

Uji pemahaman Anda dengan soal pilihan ganda yang dibuat dari catatan Anda sendiri.

**Cara Menggunakan:**

1. Masuk ke tab **Quiz**.
2. **Pilih Sumber Materi**:
   - Pilih salah satu catatan Anda dari _dropdown_.
   - Atau, jika Anda datang dari menu _Inspiration_, materi akan otomatis terpilih.
3. Ketuk tombol **Generate Quiz**.
4. AI akan membaca catatan Anda dan membuatkan 5 soal pilihan ganda.
5. Jawab soal, lihat skor, dan pelajari jawaban yang benar/salah.

---

### D. Flashcard AI

Metode belajar menghafal dengan kartu (Depan: Pertanyaan, Belakang: Jawaban).

**Cara Menggunakan:**

1. Masuk ke tab **Flashcard**.
2. Pilih catatan yang ingin dipelajari.
3. Ketuk **Buat Flashcard dengan AI**.
4. Aplikasi akan menampilkan kartu.
   - **Tap Kartu**: Untuk membalik kartu dan melihat jawaban.
   - **Swipe Kanan/Kiri** (atau tombol panah): Untuk pindah ke kartu selanjutnya/sebelumnya.

---

## 4. Catatan Teknis

- **Koneksi Internet**: Diperlukan untuk semua fitur yang memiliki tombol "AI" atau "Generate".
- **Database Lokal**: Semua catatan, kategori, dan rencana belajar disimpan secara aman di dalam memori HP Anda (SQLite). Menghapus aplikasi dapat menghapus data ini kecuali ada fitur backup (saat ini fitur Backup tertulis "dalam pengembangan").

---

_Dibuat oleh Tim Pengembang Note App._

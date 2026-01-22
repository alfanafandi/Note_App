-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Waktu pembuatan: 22 Jan 2026 pada 06.47
-- Versi server: 8.0.30
-- Versi PHP: 7.4.33

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_note_app`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `failed_jobs`
--

CREATE TABLE `failed_jobs` (
  `id` bigint UNSIGNED NOT NULL,
  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `connection` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `queue` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `payload` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `exception` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `failed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `migrations`
--

CREATE TABLE `migrations` (
  `id` int UNSIGNED NOT NULL,
  `migration` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `batch` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data untuk tabel `migrations`
--

INSERT INTO `migrations` (`id`, `migration`, `batch`) VALUES
(1, '2014_10_12_000000_create_users_table', 1),
(2, '2014_10_12_100000_create_password_reset_tokens_table', 1),
(3, '2019_08_19_000000_create_failed_jobs_table', 1),
(4, '2019_12_14_000001_create_personal_access_tokens_table', 1),
(5, '2025_12_05_101905_create_notes_table', 1);

-- --------------------------------------------------------

--
-- Struktur dari tabel `notes`
--

CREATE TABLE `notes` (
  `id` bigint UNSIGNED NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `category_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data untuk tabel `notes`
--

INSERT INTO `notes` (`id`, `title`, `content`, `category_id`, `created_at`, `updated_at`) VALUES
(2, 'Data Mining Dasar', '- Data mining adalah proses ekstraksi pola yang tersembunyi dalam kumpulan data yang besar untuk mengidentifikasi hubungan yang tidak terlihat secara langsung.\n- Tujuan dari data mining adalah menemukan informasi yang berharga dan berarti dari data yang ada.\n- Beberapa teknik data mining dasar meliputi clustering, classification, association, dan regression.\n- Contoh sederhana data mining adalah ketika sebuah perusahaan menggunakan data pelanggan untuk mengidentifikasi pola pembelian yang dapat digunakan untuk meningkatkan penjualan.', -1, '2025-12-06 01:04:28', '2025-12-06 01:04:28'),
(3, 'Pengenalan Java', 'Java adalah bahasa pemrograman yang populer digunakan untuk membuat berbagai jenis aplikasi, mulai dari aplikasi desktop hingga aplikasi web.\nJava dirancang agar mudah dipahami dan digunakan oleh para pengembang software.\nSalah satu konsep kunci dalam Java adalah OOP (Object-Oriented Programming), yang memungkinkan pengembang untuk mengorganisir kode mereka dalam bentuk objek yang memiliki atribut dan metode.\nContoh sederhana penggunaan Java adalah membuat program sederhana yang menampilkan teks \"Hello World\" pada layar.\nJava menggunakan platform-independent, artinya program Java dapat dijalankan di berbagai sistem operasi tanpa perlu melakukan perubahan kode.\nJava memiliki banyak library dan framework yang mendukung pengembangan aplikasi lebih cepat dan efisien.', 1, '2025-12-06 01:08:35', '2025-12-06 01:08:35'),
(4, 'Variabel dan Tipe Data', 'Variabel digunakan sebagai tempat untuk menyimpan nilai atau informasi dalam program komputer. Tipe data menentukan jenis nilai yang dapat disimpan dalam variabel, seperti angka, teks, boolean, dan lainnya. Contoh tipe data yang umum digunakan termasuk integer (bilangan bulat), float (bilangan desimal), string (teks), dan boolean (true/false).\n\nSebelum digunakan, variabel harus dideklarasikan dengan tipe data tertentu. Misalnya, dalam kode:\nint angka = 10; // variabel \'angka\' dengan tipe data integer dan nilai 10\nfloat harga = 5.5; // variabel \'harga\' dengan tipe data float dan nilai 5.5\nstring nama = \"Andi\"; // variabel \'nama\' dengan tipe data string dan nilai \"Andi\"\nboolean status = true; // variabel \'status\' dengan tipe data boolean dan nilai true\n\nDeklarasi variabel dengan tipe data yang tepat penting untuk memastikan bahwa nilainya sesuai dengan kebutuhan program dan untuk mencegah kesalahan saat pemrosesan data. Dengan menggunakan variabel dan tipe data yang benar, program dapat bekerja dengan efisien dan akurat.', 1, '2025-12-06 02:03:46', '2025-12-08 08:29:37'),
(5, 'Operator', 'Operator adalah simbol matematika yang digunakan untuk melakukan operasi seperti penjumlahan, pengurangan, perkalian, dan pembagian. Contoh operator penjumlahan adalah tanda tambah (+), contoh pengurangan adalah tanda minus (-), contoh perkalian adalah tanda kali (x), dan contoh pembagian adalah tanda bagi (÷).\n\nSelain operator matematika dasar, ada juga operator lain seperti operator perbandingan (misalnya sama dengan, lebih besar dari, kurang dari), operator logika (misalnya AND, OR, NOT), dan operator assignment (misalnya =, +=, -=). Operator perbandingan digunakan untuk membandingkan dua nilai dan menghasilkan nilai kebenaran (true/false), operator logika digunakan untuk mengkombinasikan nilai kebenaran, sedangkan operator assignment digunakan untuk memberikan nilai ke suatu variabel.\n\nOperator matematika sangat penting karena digunakan untuk mempermudah perhitungan dan pengolahan data dalam berbagai bidang, seperti ilmu pengetahuan, teknologi, dan ekonomi. Dengan menggunakan operator matematika, kita dapat melakukan berbagai macam operasi matematika secara efisien dan akurat, sehingga memudahkan dalam analisis data dan pemecahan masalah.\ns\nS\nS\nS\nS\nS\n\nS\nS\ns', 1, '2025-12-06 02:04:51', '2025-12-09 06:41:49'),
(6, 'Array', '- Array adalah struktur data yang dapat menyimpan kumpulan nilai dengan tipe data yang sama dalam satu variabel.\n- Array memiliki indeks yang dimulai dari 0, yang digunakan untuk mengakses nilai tertentu di dalamnya.\n- Contoh deklarasi array: int[] angka = new int[5]; // Mendeklarasikan array integer dengan panjang 5.\n- Contoh inisialisasi array: int[] angka = {1, 2, 3, 4, 5}; // Menginisialisasi array dengan nilai tertentu.\n- Contoh mengakses nilai array: int nilaiPertama = angka[0]; // Mengakses nilai pertama dalam array \'angka\'.', 1, '2025-12-08 18:53:16', '2025-12-08 18:53:16'),
(8, 'Pengertian Pancasila', '- Pancasila adalah dasar negara dan ideologi nasional Indonesia.\n- Pancasila berasal dari kata Sanskerta, terdiri dari \"panca\" yang berarti lima dan \"sila\" yang berarti prinsip atau asas.\n- Terdiri dari lima sila, yaitu Ketuhanan Yang Maha Esa, Kemanusiaan yang Adil dan Beradab, Persatuan Indonesia, Kerakyatan yang Dipimpin oleh Hikmat Kebijaksanaan dalam Permusyawaratan/Perwakilan, dan Keadilan Sosial bagi seluruh rakyat Indonesia.\n- Pancasila menjadi pedoman bagi kehidupan bermasyarakat, berbangsa, dan bernegara di Indonesia.\n- Contoh penerapan Pancasila adalah dalam kehidupan sehari-hari seperti gotong royong, toleransi antar umat beragama, dan semangat persatuan dan kesatuan di Indonesia.', 2, '2026-01-13 22:55:00', '2026-01-13 22:55:00'),
(9, 'Sejarah Pembentukan Pancasila', '1. Pancasila dibentuk oleh Bung Karno sebagai dasar negara Indonesia pada tanggal 1 Juni 1945.\n2. Sejarah pembentukan Pancasila berawal dari upaya untuk mencari nilai-nilai yang dapat diterima oleh semua elemen masyarakat Indonesia.\n3. Proses pembentukan Pancasila melibatkan pertemuan para tokoh dan pemimpin nasional seperti Soekarno, Hatta, dan Ki Hajar Dewantara.\n4. Pancasila terdiri dari lima sila yang masing-masing melambangkan nilai-nilai fundamental bangsa Indonesia, yaitu Ketuhanan Yang Maha Esa, Kemanusiaan yang Adil dan Beradab, Persatuan Indonesia, Kerakyatan yang Dipimpin oleh Hikmat Kebijaksanaan dalam Permusyawaratan/Perwakilan, dan Keadilan Sosial bagi Seluruh Rakyat Indonesia.', 2, '2026-01-13 22:59:14', '2026-01-13 22:59:14');

-- --------------------------------------------------------

--
-- Struktur dari tabel `password_reset_tokens`
--

CREATE TABLE `password_reset_tokens` (
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `personal_access_tokens`
--

CREATE TABLE `personal_access_tokens` (
  `id` bigint UNSIGNED NOT NULL,
  `tokenable_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tokenable_id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `abilities` text COLLATE utf8mb4_unicode_ci,
  `last_used_at` timestamp NULL DEFAULT NULL,
  `expires_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Struktur dari tabel `users`
--

CREATE TABLE `users` (
  `id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email_verified_at` timestamp NULL DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `remember_token` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `failed_jobs`
--
ALTER TABLE `failed_jobs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `failed_jobs_uuid_unique` (`uuid`);

--
-- Indeks untuk tabel `migrations`
--
ALTER TABLE `migrations`
  ADD PRIMARY KEY (`id`);

--
-- Indeks untuk tabel `notes`
--
ALTER TABLE `notes`
  ADD PRIMARY KEY (`id`);

--
-- Indeks untuk tabel `password_reset_tokens`
--
ALTER TABLE `password_reset_tokens`
  ADD PRIMARY KEY (`email`);

--
-- Indeks untuk tabel `personal_access_tokens`
--
ALTER TABLE `personal_access_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `personal_access_tokens_token_unique` (`token`),
  ADD KEY `personal_access_tokens_tokenable_type_tokenable_id_index` (`tokenable_type`,`tokenable_id`);

--
-- Indeks untuk tabel `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `users_email_unique` (`email`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `failed_jobs`
--
ALTER TABLE `failed_jobs`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `migrations`
--
ALTER TABLE `migrations`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT untuk tabel `notes`
--
ALTER TABLE `notes`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT untuk tabel `personal_access_tokens`
--
ALTER TABLE `personal_access_tokens`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

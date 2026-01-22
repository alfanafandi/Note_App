package com.example.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    // 🔥 NAIKKAN VERSI DATABASE ke 8
    private static final int DATABASE_VERSION = 8;

    // =============================
    // Tabel Catatan (TETAP SAMA)
    // =============================
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    private static final String COLUMN_CATEGORY_ID = "category_id";

    // =============================
    // Tabel Kategori (TETAP SAMA)
    // =============================
    private static final String TABLE_CATEGORIES = "categories";
    private static final String CATEGORY_ID = "id";
    private static final String CATEGORY_NAME = "name";


    // =============================
    // Tabel Mind Maps
    // =============================
    public static final String TABLE_MIND_MAPS = "mind_maps";
    public static final String COLUMN_MM_TOPIC_TITLE = "topic_title";
    public static final String COLUMN_MM_IS_ACTIVE = "is_active";

    // =============================
    // Tabel Sub-Topik Mind Map
    // =============================
    public static final String TABLE_MIND_MAP_TOPICS = "mind_map_topics";
    public static final String COLUMN_MMT_MIND_MAP_ID = "mind_map_id";
    public static final String COLUMN_MMT_SUB_TOPIC_TITLE = "sub_topic_title";
    public static final String COLUMN_MMT_IS_COMPLETED = "is_completed";
    // 🔥 BARU: Kolom untuk menyimpan konten materi AI
    public static final String COLUMN_MMT_CONTENT = "sub_topic_content";


    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Kode onCreate Anda yang lama tetap sama
        String createCategories = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CATEGORY_NAME + " TEXT UNIQUE)";
        db.execSQL(createCategories);

        String createNotes = "CREATE TABLE " + TABLE_NOTES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_CREATED_AT + " TEXT, " +
                COLUMN_UPDATED_AT + " TEXT, " +
                COLUMN_CATEGORY_ID + " INTEGER, " +
                "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " +
                TABLE_CATEGORIES + "(" + CATEGORY_ID + "))";
        db.execSQL(createNotes);

        // Pembuatan tabel mind map
        String CREATE_MIND_MAPS_TABLE = "CREATE TABLE " + TABLE_MIND_MAPS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MM_TOPIC_TITLE + " TEXT,"
                + COLUMN_MM_IS_ACTIVE + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_MIND_MAPS_TABLE);

        // 🔥 PERBARUI: Pembuatan tabel sub-topik dengan kolom konten
        String CREATE_MIND_MAP_TOPICS_TABLE = "CREATE TABLE " + TABLE_MIND_MAP_TOPICS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MMT_MIND_MAP_ID + " INTEGER,"
                + COLUMN_MMT_SUB_TOPIC_TITLE + " TEXT,"
                + COLUMN_MMT_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + COLUMN_MMT_CONTENT + " TEXT," // <-- Kolom baru ditambahkan
                + "FOREIGN KEY(" + COLUMN_MMT_MIND_MAP_ID + ") REFERENCES " + TABLE_MIND_MAPS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_MIND_MAP_TOPICS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_CREATED_AT + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_UPDATED_AT + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_CATEGORY_ID + " INTEGER DEFAULT NULL");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" +
                    CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CATEGORY_NAME + " TEXT UNIQUE)");
        }
        if (oldVersion < 7) {
            String CREATE_MIND_MAPS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MIND_MAPS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_MM_TOPIC_TITLE + " TEXT,"
                    + COLUMN_MM_IS_ACTIVE + " INTEGER DEFAULT 0"
                    + ")";
            db.execSQL(CREATE_MIND_MAPS_TABLE);

            String CREATE_MIND_MAP_TOPICS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MIND_MAP_TOPICS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_MMT_MIND_MAP_ID + " INTEGER,"
                    + COLUMN_MMT_SUB_TOPIC_TITLE + " TEXT,"
                    + COLUMN_MMT_IS_COMPLETED + " INTEGER DEFAULT 0,"
                    + "FOREIGN KEY(" + COLUMN_MMT_MIND_MAP_ID + ") REFERENCES " + TABLE_MIND_MAPS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
                    + ")";
            db.execSQL(CREATE_MIND_MAP_TOPICS_TABLE);
            Log.d("DBHelper", "Database di-upgrade ke versi 7, tabel mind map dibuat.");
        }
        // 🔥 BARU: Logika upgrade untuk versi 8
        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE " + TABLE_MIND_MAP_TOPICS + " ADD COLUMN " + COLUMN_MMT_CONTENT + " TEXT");
            Log.d("DBHelper", "Database di-upgrade ke versi 8, kolom konten sub-topik ditambahkan.");
        }
    }

    // ====================================================================
    // METODE UNTUK NOTES & CATEGORIES (TIDAK BERUBAH)
    // ====================================================================

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT n.*, c.name AS category_name FROM " + TABLE_NOTES + " n " +
                        "LEFT JOIN " + TABLE_CATEGORIES + " c ON n." + COLUMN_CATEGORY_ID + " = c." + CATEGORY_ID +
                        " ORDER BY n.updated_at DESC", null);

        if (cursor.moveToFirst()) {
            do {
                notes.add(cursorToNote(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }

    public Note getNoteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT n.*, c.name AS category_name FROM " + TABLE_NOTES + " n " +
                        "LEFT JOIN " + TABLE_CATEGORIES + " c ON n." + COLUMN_CATEGORY_ID + " = c." + CATEGORY_ID +
                        " WHERE n." + COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        Note note = null;
        if (cursor.moveToFirst()) {
            note = cursorToNote(cursor);
        }
        cursor.close();
        db.close();
        return note;
    }

    // DBHelper.java

    // 🔥🔥 METHOD BARU YANG DITAMBAHKAN 🔥🔥
    public String getSubTopicTitle(int subTopicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String title = null;
        try {
            cursor = db.query(TABLE_MIND_MAP_TOPICS,
                    new String[]{COLUMN_MMT_SUB_TOPIC_TITLE}, // Kolom yang ingin diambil
                    COLUMN_ID + "=?", // Seleksi berdasarkan ID
                    new String[]{String.valueOf(subTopicId)}, // Argumen seleksi
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MMT_SUB_TOPIC_TITLE));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return title;
    }


    public boolean insertNote(String title, String content, @Nullable Integer categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_CREATED_AT, dateFormat.format(new Date()));
        values.put(COLUMN_UPDATED_AT, dateFormat.format(new Date()));

        if (categoryId != null && categoryId > 0) {
            values.put(COLUMN_CATEGORY_ID, categoryId);
        } else {
            values.putNull(COLUMN_CATEGORY_ID);
        }

        long result = db.insert(TABLE_NOTES, null, values);
        db.close();
        return result != -1;
    }

    public boolean updateNote(int id, String title, String content, @Nullable Integer categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_UPDATED_AT, dateFormat.format(new Date()));

        if (categoryId != null && categoryId > 0) {
            values.put(COLUMN_CATEGORY_ID, categoryId);
        } else {
            values.putNull(COLUMN_CATEGORY_ID);
        }

        int rows = db.update(TABLE_NOTES, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public boolean deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NOTES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public List<Note> getNotesByCategory(int categoryId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT n.*, c.name AS category_name FROM " + TABLE_NOTES + " n " +
                        "LEFT JOIN " + TABLE_CATEGORIES + " c ON n." + COLUMN_CATEGORY_ID + " = c." + CATEGORY_ID +
                        " WHERE n." + COLUMN_CATEGORY_ID + " = ? ORDER BY n.updated_at DESC",
                new String[]{String.valueOf(categoryId)}
        );

        if (cursor.moveToFirst()) {
            do {
                notes.add(cursorToNote(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notes;
    }

    public List<Note> getNotesWithoutCategory() {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT n.*, c.name AS category_name FROM " + TABLE_NOTES + " n " +
                        "LEFT JOIN " + TABLE_CATEGORIES + " c ON n." + COLUMN_CATEGORY_ID + " = c." + CATEGORY_ID +
                        " WHERE n." + COLUMN_CATEGORY_ID + " IS NULL OR n." + COLUMN_CATEGORY_ID + " = 0 " +
                        "ORDER BY n.updated_at DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                notes.add(cursorToNote(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notes;
    }

    private Note cursorToNote(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
        String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
        String updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT));
        int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"));
        return new Note(id, title, content, createdAt, updatedAt, categoryId, categoryName);
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, CATEGORY_NAME + " ASC");
        if (cursor.moveToFirst()) {
            do {
                categories.add(new Category(
                        cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_NAME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }

    public boolean addCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, name);
        long result = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return result != -1;
    }

    public long addCategoryAndGetId(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, name);
        long id = db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{CATEGORY_ID}, CATEGORY_NAME + "=?", new String[]{name}, null, null, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID));
            }
            cursor.close();
        }
        db.close();
        return id;
    }

    public int getCategoryIdByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{CATEGORY_ID}, CATEGORY_NAME + "=?", new String[]{name}, null, null, null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID));
        }
        cursor.close();
        db.close();
        return id;
    }

    public boolean updateCategory(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CATEGORY_NAME, name);
        int rows = db.update(TABLE_CATEGORIES, values, CATEGORY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    // DBHelper.java
// 🔥 GANTI FUNGSI LAMA DENGAN YANG INI
    public boolean deleteCategory(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Langkah 1: Buat objek ContentValues secara normal
        ContentValues values = new ContentValues();
        values.putNull(COLUMN_CATEGORY_ID);

        // Langkah 2: Update semua catatan yang memiliki kategori ini, set category_id menjadi null
        db.update(TABLE_NOTES, values, COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(id)});

        // Langkah 3: Hapus kategori dari tabel kategori
        int rows = db.delete(TABLE_CATEGORIES, CATEGORY_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
        return rows > 0;
    }


    // ====================================================================
    // 🔥 METODE BARU DAN YANG DIPERBARUI UNTUK MIND MAP & UX BARU
    // ====================================================================

    public long createMindMap(String topic, List<String> subTopics) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long mindMapId = -1;
        try {
            // Nonaktifkan semua mind map yang ada
            db.execSQL("UPDATE " + TABLE_MIND_MAPS + " SET " + COLUMN_MM_IS_ACTIVE + " = 0");

            ContentValues values = new ContentValues();
            values.put(COLUMN_MM_TOPIC_TITLE, topic);
            values.put(COLUMN_MM_IS_ACTIVE, 1);
            mindMapId = db.insert(TABLE_MIND_MAPS, null, values);

            if (mindMapId != -1) {
                for (String subTopic : subTopics) {
                    ContentValues subValues = new ContentValues();
                    subValues.put(COLUMN_MMT_MIND_MAP_ID, mindMapId);
                    subValues.put(COLUMN_MMT_SUB_TOPIC_TITLE, subTopic);
                    subValues.put(COLUMN_MMT_IS_COMPLETED, 0); // Default belum selesai
                    db.insert(TABLE_MIND_MAP_TOPICS, null, subValues);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        return mindMapId;
    }

    public MindMap getActiveMindMap() {
        SQLiteDatabase db = this.getReadableDatabase();
        MindMap mindMap = null;

        Cursor cursor = db.query(TABLE_MIND_MAPS, null, COLUMN_MM_IS_ACTIVE + " = ?", new String[]{"1"}, null, null, null);

        if (cursor.moveToFirst()) {
            int mindMapId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MM_TOPIC_TITLE));
            List<MindMap.SubTopic> subTopics = new ArrayList<>();

            // 🔥 PERBARUI QUERY: Ambil juga kolom konten
            Cursor topicCursor = db.query(TABLE_MIND_MAP_TOPICS,
                    new String[]{COLUMN_ID, COLUMN_MMT_SUB_TOPIC_TITLE, COLUMN_MMT_IS_COMPLETED, COLUMN_MMT_CONTENT},
                    COLUMN_MMT_MIND_MAP_ID + " = ?",
                    new String[]{String.valueOf(mindMapId)},
                    null, null, null);

            if (topicCursor.moveToFirst()) {
                do {
                    int subId = topicCursor.getInt(topicCursor.getColumnIndexOrThrow(COLUMN_ID));
                    String subTitle = topicCursor.getString(topicCursor.getColumnIndexOrThrow(COLUMN_MMT_SUB_TOPIC_TITLE));
                    boolean isCompleted = topicCursor.getInt(topicCursor.getColumnIndexOrThrow(COLUMN_MMT_IS_COMPLETED)) == 1;
                    // 🔥 BARU: Ambil konten dari cursor
                    String content = topicCursor.getString(topicCursor.getColumnIndexOrThrow(COLUMN_MMT_CONTENT));

                    // 🔥 PERBARUI: Gunakan constructor baru
                    subTopics.add(new MindMap.SubTopic(subId, subTitle, content, isCompleted));
                } while (topicCursor.moveToNext());
            }
            topicCursor.close();
            mindMap = new MindMap(mindMapId, title, true, subTopics);
        }
        cursor.close();
        db.close();
        return mindMap;
    }

    public void updateSubTopicStatus(int subTopicId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MMT_IS_COMPLETED, isCompleted ? 1 : 0);
        db.update(TABLE_MIND_MAP_TOPICS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(subTopicId)});
        db.close();
    }

    public void deleteAllMindMaps() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Karena ada ON DELETE CASCADE, menghapus mind map akan otomatis menghapus sub-topiknya.
        db.delete(TABLE_MIND_MAPS, null, null);
        db.close();
    }

    // 🔥 FUNGSI BARU 1: Untuk menyimpan konten AI
    public void updateSubTopicContent(int subTopicId, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MMT_CONTENT, content);
        db.update(TABLE_MIND_MAP_TOPICS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(subTopicId)});
        db.close();
    }

    // 🔥 FUNGSI BARU 2: Untuk mengambil konten saat item ditampilkan
    public String getSubTopicContent(int subTopicId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String content = null;
        Cursor cursor = db.query(TABLE_MIND_MAP_TOPICS,
                new String[]{COLUMN_MMT_CONTENT},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(subTopicId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int contentIndex = cursor.getColumnIndex(COLUMN_MMT_CONTENT);
            if (contentIndex != -1) {
                content = cursor.getString(contentIndex);
            }
            cursor.close();
        }
        db.close();
        return content;
    }

    // ... (Tambahkan kode di bawah ini di dalam kelas DBHelper, misalnya setelah fungsi getSubTopicContent())

    // 🔥🔥 FUNGSI BARU UNTUK MENGAMBIL SEMUA JUDUL MIND MAP 🔥🔥
    public List<MindMap> getAllMindMaps() {
        List<MindMap> mindMapList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Mengurutkan berdasarkan ID terbaru di atas
        Cursor cursor = db.query(TABLE_MIND_MAPS, null, null, null, null, null, COLUMN_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MM_TOPIC_TITLE));
                boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MM_IS_ACTIVE)) == 1;
                // Kita tidak perlu load sub-topiknya di sini, cukup judulnya untuk ditampilkan di dialog
                mindMapList.add(new MindMap(id, title, isActive, new ArrayList<>()));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return mindMapList;
    }

    // 🔥🔥 FUNGSI BARU UNTUK MENGGANTI MIND MAP AKTIF 🔥🔥
    public void setActiveMindMap(int mindMapId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Nonaktifkan semua rencana yang ada
            db.execSQL("UPDATE " + TABLE_MIND_MAPS + " SET " + COLUMN_MM_IS_ACTIVE + " = 0");

            // 2. Aktifkan rencana yang dipilih berdasarkan ID-nya
            ContentValues values = new ContentValues();
            values.put(COLUMN_MM_IS_ACTIVE, 1);
            db.update(TABLE_MIND_MAPS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(mindMapId)});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // 🔥🔥 FUNGSI BARU UNTUK MENGHAPUS MIND MAP BERDASARKAN ID 🔥🔥
    public void deleteMindMap(int mindMapId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Karena kita sudah set ON DELETE CASCADE, sub-topik akan ikut terhapus
        db.delete(TABLE_MIND_MAPS, COLUMN_ID + " = ?", new String[]{String.valueOf(mindMapId)});
        db.close();
    }

}

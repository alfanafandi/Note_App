package com.example.note;

import android.content.Context;

import com.example.note.network.NoteApi;
import com.example.note.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;import retrofit2.Response;

public class NoteRepository {

    // ==========================================================
    // == SAKLAR UTAMA (Ganti 'true' atau 'false' di sini) ==
    // ==========================================================
    private static final boolean USE_API = true; // true = Online (API), false = Offline (SQLite)
    // ==========================================================

    private final NoteApi noteApi;
    private final DBHelper dbHelper;

    public NoteRepository(Context context) {
        this.noteApi = RetrofitClient.getClient();
        this.dbHelper = new DBHelper(context);
    }

    public void getAllNotes(Callback<List<Note>> callback) {
        if (USE_API) {
            noteApi.getAllNotes().enqueue(callback);
        } else {
            List<Note> localNotes = dbHelper.getAllNotes();
            Call<List<Note>> fakeCall = new FakeCall<>(localNotes);
            callback.onResponse(fakeCall, Response.success(localNotes));
        }
    }

    public void getNoteById(int noteId, Callback<Note> callback) {
        if (USE_API) {
            // -- MODE ONLINE --
            noteApi.getNoteById(noteId).enqueue(callback);
        } else {
            // -- MODE OFFLINE --
            Note localNote = dbHelper.getNoteById(noteId);
            Call<Note> fakeCall = new FakeCall<>(localNote);
            if (localNote != null) {
                callback.onResponse(fakeCall, Response.success(localNote));
            } else {
                callback.onFailure(fakeCall, new Exception("Catatan dengan ID " + noteId + " tidak ditemukan di DB lokal."));
            }
        }
    }


    /**
     * Menambahkan catatan baru.
     */
    public void addNote(Note note, Callback<Note> callback) {
        if (USE_API) {
            noteApi.createNote(note).enqueue(callback);
        } else {
            boolean success = dbHelper.insertNote(note.getTitle(), note.getContent(), note.getCategoryId());
            if (success) {
                callback.onResponse(new FakeCall<>(note), Response.success(note));
            } else {
                callback.onFailure(new FakeCall<>(null), new Exception("Gagal menyimpan ke database lokal"));
            }
        }
    }

    /**
     * Mengupdate catatan yang sudah ada.
     */
    public void updateNote(int noteId, Note note, Callback<Void> callback) {
        if (USE_API) {
            noteApi.updateNote(noteId, note).enqueue(new Callback<Note>() {
                @Override
                public void onResponse(Call<Note> call, Response<Note> response) {
                    if (response.isSuccessful()) {
                        callback.onResponse(new FakeCall<>(null), Response.success(null));
                    } else {
                        callback.onFailure(new FakeCall<>(null), new Exception("Gagal update API: " + response.message()));
                    }
                }

                @Override
                public void onFailure(Call<Note> call, Throwable t) {
                    callback.onFailure(new FakeCall<>(null), t);
                }
            });

        } else {
            boolean success = dbHelper.updateNote(noteId, note.getTitle(), note.getContent(), note.getCategoryId());
            if (success) {
                callback.onResponse(new FakeCall<>(null), Response.success(null));
            } else {
                callback.onFailure(new FakeCall<>(null), new Exception("Gagal update ke database lokal"));
            }
        }
    }


    /**
     * Menghapus catatan.
     */
    public void deleteNote(int noteId, Callback<Void> callback) {
        if (USE_API) {
            noteApi.deleteNote(noteId).enqueue(callback);
        } else {
            boolean success = dbHelper.deleteNote(noteId);
            if (success) {
                callback.onResponse(new FakeCall<>(null), Response.success(null));
            } else {
                callback.onFailure(new FakeCall<>(null), new Exception("Gagal menghapus dari database lokal"));
            }
        }
    }

    // Kelas helper untuk mensimulasikan Retrofit Call.
    private static class FakeCall<T> implements Call<T> {
        private final T response;
        private boolean executed = false;

        FakeCall(T response) {
            this.response = response;
        }

        @Override
        public Response<T> execute() { return Response.success(response); }
        @Override
        public void enqueue(Callback<T> callback) { }
        @Override public boolean isExecuted() { return executed; }
        @Override public void cancel() {}
        @Override public boolean isCanceled() { return false; }
        @Override public Call<T> clone() { return new FakeCall<>(response); }
        @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("http://localhost/fake").build(); }
        @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
    }
}

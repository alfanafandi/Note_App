package com.example.note.network;

import com.example.note.Note;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NoteApi {

    // GET /api/notes
    @GET("notes")
    Call<List<Note>> getAllNotes();

    // GET /api/notes/{id}
    @GET("notes/{id}")
    Call<Note> getNoteById(@Path("id") int noteId);

    // POST /api/notes
    @POST("notes")
    Call<Note> createNote(@Body Note note);

    // PUT /api/notes/{id}
    @PUT("notes/{id}")
    Call<Note> updateNote(@Path("id") int noteId, @Body Note note);

    // DELETE /api/notes/{id}
    @DELETE("notes/{id}")
    Call<Void> deleteNote(@Path("id") int noteId);
}

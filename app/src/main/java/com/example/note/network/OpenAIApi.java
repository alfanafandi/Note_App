package com.example.note.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OpenAIApi {

    /**
     * Versi ini digunakan untuk OpenRouter.
     * Header Authorization ditangani oleh Interceptor di DetailNoteActivity.
     */
    @POST("chat/completions")
    Call<OpenAIResponse> createChatCompletion(@Body OpenAIRequest request);

}

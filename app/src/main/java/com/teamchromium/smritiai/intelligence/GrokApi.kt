package com.teamchromium.smritiai.intelligence

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GrokApi {

    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GrokRequest
    ): GrokResponse
}
package com.teamchromium.smritiai.intelligence

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GrokClient {

    private val okHttpClient = OkHttpClient.Builder().build()

    val api: GrokApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.x.ai/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GrokApi::class.java)
    }
}
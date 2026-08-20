package com.agriconnect.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Use your PC's IP for local testing, or the production URL for deployment
    private const val BASE_URL = "https://agriconnect-backend-2jig.onrender.com/"

    // Local AI Service (Run main.py in agri-ai-server)
    private const val AI_BASE_URL = "http://192.168.1.5:5001/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: AgriConnectApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(AgriConnectApi::class.java)
    }

    val aiAssistant: VoiceAssistantApi by lazy {
        Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(VoiceAssistantApi::class.java)
    }
}

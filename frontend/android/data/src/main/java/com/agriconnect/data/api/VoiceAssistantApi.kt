package com.agriconnect.data.api

import com.agriconnect.data.model.AIActionResponse
import com.agriconnect.data.model.AIRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface VoiceAssistantApi {

    @Multipart
    @POST("assistant/voice")
    suspend fun processVoice(
        @Part audio: MultipartBody.Part,
        @Part("context") context: String // JSON string
    ): Response<Map<String, Any>>

    @POST("ai/process")
    suspend fun processText(
        @Body request: AIRequest
    ): Response<AIActionResponse>

    @Multipart
    @POST("ai/whisper")
    suspend fun transcribeAudio(
        @Part audio: MultipartBody.Part
    ): Response<Map<String, String>> // e.g. {"text": "..."}
}

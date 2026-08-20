package com.agriconnect.app.ui.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.agriconnect.data.api.RetrofitClient
import com.agriconnect.data.model.AIActionResponse
import com.agriconnect.data.model.AIContext
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class VoiceAssistantManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val gson = Gson()
    private val aiBaseUrl = "http://192.168.1.5:5001/" // Local AI Server

    suspend fun processVoice(audioFile: File, aiContext: AIContext): VoiceAssistantResult? = withContext(Dispatchers.IO) {
        try {
            val requestFile = audioFile.asRequestBody("audio/mpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
            val contextPart = gson.toJson(aiContext)
            
            val response = RetrofitClient.aiAssistant.processVoice(body, contextPart)
            
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                
                val result = VoiceAssistantResult(
                    transcript = data["transcript"] as? String ?: "",
                    action = data["action"] as? String,
                    type = data["type"] as? String,
                    response = data["response"] as? String ?: "",
                    arguments = data["arguments"] as? Map<String, Any> ?: emptyMap(),
                    audioUrl = data["audio_url"] as? String
                )
                
                return@withContext result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    fun playResponse(audioUrl: String, onComplete: () -> Unit = {}) {
        mediaPlayer?.release()
        val fullUrl = if (audioUrl.startsWith("http")) audioUrl else "${aiBaseUrl.removeSuffix("/")}${audioUrl}"
        
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.parse(fullUrl))
            prepareAsync()
            setOnPreparedListener { start() }
            setOnCompletionListener { 
                onComplete()
                release()
                mediaPlayer = null
            }
            setOnErrorListener { _, _, _ ->
                onComplete()
                false
            }
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

data class VoiceAssistantResult(
    val transcript: String,
    val action: String?,
    val type: String?,
    val response: String,
    val arguments: Map<String, Any>,
    val audioUrl: String?
)

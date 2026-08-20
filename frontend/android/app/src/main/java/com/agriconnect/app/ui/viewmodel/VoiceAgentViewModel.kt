package com.agriconnect.app.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.data.api.RetrofitClient
import com.agriconnect.data.model.AIActionResponse
import com.agriconnect.data.model.AIContext
import com.agriconnect.data.model.AIRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

import android.speech.tts.TextToSpeech
import java.util.*
import com.agriconnect.app.ui.utils.VoiceAssistantManager

enum class AssistantState {
    IDLE,
    LISTENING,
    TRANSCRIBING,
    THINKING,
    RESPONDING,
    ERROR
}

class VoiceAgentViewModel(context: android.content.Context) : ViewModel() {
    
    companion object {
        fun provideFactory(context: android.content.Context): androidx.lifecycle.ViewModelProvider.Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return VoiceAgentViewModel(context.applicationContext) as T
            }
        }
    }

    private var tts: TextToSpeech? = null
    private val assistantManager = VoiceAssistantManager(context)
    
    init {
        initTTS(context)
    }
    
    fun initTTS(context: android.content.Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale("te", "IN")
                }
            }
        }
    }

    private val _state = mutableStateOf(AssistantState.IDLE)
    val state: State<AssistantState> = _state

    private val _responseText = mutableStateOf("")
    val responseText: State<String> = _responseText

    private val _transcribedText = mutableStateOf("")
    val transcribedText: State<String> = _transcribedText

    private val _lastAction = mutableStateOf<AIActionResponse?>(null)
    val lastAction: State<AIActionResponse?> = _lastAction

    fun startListening() {
        _state.value = AssistantState.LISTENING
        _responseText.value = ""
        _transcribedText.value = ""
        assistantManager.stop()
    }

    fun stopListening(audioFile: File?, context: AIContext, onAction: (AIActionResponse) -> Unit) {
        if (audioFile == null) {
            _state.value = AssistantState.IDLE
            return
        }

        _state.value = AssistantState.TRANSCRIBING
        viewModelScope.launch {
            try {
                android.util.Log.d("VoiceAI", "Uploading audio: ${audioFile.absolutePath}")
                
                val result = assistantManager.processVoice(audioFile, context)
                
                if (result != null) {
                    _transcribedText.value = result.transcript
                    _responseText.value = result.response
                    
                    if (result.audioUrl != null) {
                        _state.value = AssistantState.RESPONDING
                        assistantManager.playResponse(result.audioUrl) {
                            // On complete
                            _state.value = AssistantState.IDLE
                        }
                    } else {
                        _state.value = AssistantState.IDLE
                    }

                    // Execute action if provided
                    if (result.action != null) {
                        onAction(AIActionResponse(
                            action = result.action,
                            responseText = result.response,
                            target = result.arguments["target"] as? String,
                            requiresConfirmation = false
                        ))
                    }
                } else {
                    _state.value = AssistantState.ERROR
                    _responseText.value = "Speech recognition failed."
                    tts?.speak(_responseText.value, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            } catch (e: Exception) {
                android.util.Log.e("VoiceAI", "Error in stopListening", e)
                _state.value = AssistantState.ERROR
                _responseText.value = "Sorry, I couldn't hear you."
                tts?.speak(_responseText.value, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    fun processText(text: String, context: AIContext, onAction: (AIActionResponse) -> Unit) {
        _state.value = AssistantState.THINKING
        _transcribedText.value = text
        
        viewModelScope.launch {
            try {
                android.util.Log.d("VoiceAI", "Processing intent for: $text")
                val response = RetrofitClient.aiAssistant.processText(AIRequest(text, context))
                if (response.isSuccessful && response.body() != null) {
                    val aiAction = response.body()!!
                    android.util.Log.d("VoiceAI", "AI Action: ${aiAction.action}")
                    _lastAction.value = aiAction
                    _responseText.value = aiAction.responseText
                    _state.value = AssistantState.RESPONDING
                    
                    // Speak the response in Telugu
                    tts?.speak(aiAction.responseText, TextToSpeech.QUEUE_FLUSH, null, null)

                    if (!aiAction.requiresConfirmation) {
                        onAction(aiAction)
                    }
                } else {
                    android.util.Log.e("VoiceAI", "AI Process failed: ${response.code()}")
                    _state.value = AssistantState.ERROR
                    _responseText.value = "I'm having trouble connecting to my brain."
                    tts?.speak(_responseText.value, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            } catch (e: Exception) {
                android.util.Log.e("VoiceAI", "Error in processText", e)
                _state.value = AssistantState.ERROR
                _responseText.value = "Network error. Is the AI server running?"
                tts?.speak(_responseText.value, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    fun confirmAction(onAction: (AIActionResponse) -> Unit) {
        lastAction.value?.let {
            onAction(it)
            _state.value = AssistantState.IDLE
            _lastAction.value = null
        }
    }

    fun dismiss() {
        _state.value = AssistantState.IDLE
        _responseText.value = ""
        _lastAction.value = null
        tts?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}

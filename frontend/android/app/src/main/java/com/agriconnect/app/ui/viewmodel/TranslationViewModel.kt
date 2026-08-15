package com.agriconnect.app.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.app.data.SessionManager
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class TranslationViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    
    private val _currentLanguage = mutableStateOf(sessionManager.getLanguage())
    val currentLanguage: State<String> = _currentLanguage

    private val _isModelDownloading = mutableStateOf(false)
    val isModelDownloading: State<Boolean> = _isModelDownloading

    private var translator: Translator? = null
    
    // Cache for translations to avoid redundant ML Kit calls
    private val translationCache = mutableStateMapOf<String, String>()

    init {
        setupTranslator(_currentLanguage.value)
    }

    fun setLanguage(languageCode: String) {
        if (_currentLanguage.value == languageCode) return
        
        _currentLanguage.value = languageCode
        sessionManager.saveLanguage(languageCode)
        translationCache.clear()
        setupTranslator(languageCode)
    }

    private fun setupTranslator(targetLanguageCode: String) {
        translator?.close()
        
        if (targetLanguageCode == "en") {
            translator = null
            return
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(if (targetLanguageCode == "te") TranslateLanguage.TELUGU else TranslateLanguage.ENGLISH)
            .build()
        
        translator = Translation.getClient(options)
        
        viewModelScope.launch {
            _isModelDownloading.value = true
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            
            try {
                translator?.downloadModelIfNeeded(conditions)?.await()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isModelDownloading.value = false
            }
        }
    }

    fun translate(text: String, onResult: (String) -> Unit) {
        val targetLang = _currentLanguage.value
        if (targetLang == "en" || text.isEmpty()) {
            onResult(text)
            return
        }

        if (translationCache.containsKey(text)) {
            onResult(translationCache[text]!!)
            return
        }

        val currentTranslator = translator
        if (currentTranslator != null) {
            viewModelScope.launch {
                try {
                    val result = currentTranslator.translate(text).await()
                    translationCache[text] = result
                    onResult(result)
                } catch (e: Exception) {
                    onResult(text)
                }
            }
        } else {
            onResult(text)
        }
    }

    override fun onCleared() {
        super.onCleared()
        translator?.close()
    }
}

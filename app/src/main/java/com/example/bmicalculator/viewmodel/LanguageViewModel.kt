package com.example.bmicalculator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.util.LangHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow("")
    val uiState: StateFlow<String> = _uiState.asStateFlow()


    sealed class LanguageIntent {
        object SwitchChinese : LanguageIntent()
        object SwitchEnglish : LanguageIntent()
        object NavToBack : LanguageIntent()
        data class LoadLanguage(val context: Context) : LanguageIntent()
    }

    fun processIntent(intent: LanguageIntent) {
        when (intent) {
            is LanguageIntent.SwitchEnglish -> {
                setLanguage(LangHelper.LANG_EN)
                emitEvent(LanguageEvent.SwitchEnglish)
            }

            is LanguageIntent.SwitchChinese -> {
                setLanguage(LangHelper.LANG_ZH)
                emitEvent(LanguageEvent.SwitchChinese)
            }

            is LanguageIntent.NavToBack -> emitEvent(LanguageEvent.NavToBack)
            is LanguageIntent.LoadLanguage -> loadSavedLang(intent.context)
        }
    }

    sealed class LanguageEvent {
        object NavToBack : LanguageEvent()
        object SwitchChinese : LanguageEvent()
        object SwitchEnglish : LanguageEvent()
    }

    private val _event = MutableSharedFlow<LanguageEvent>()
    val event: SharedFlow<LanguageEvent> = _event.asSharedFlow()

    private fun emitEvent(newEvent: LanguageEvent) {
        viewModelScope.launch {
            _event.emit(newEvent)
        }
    }

    private fun setLanguage(language: String) {
        _uiState.value = language
    }

    // 提供加载方法
    private fun loadSavedLang(context: Context) {
        val appContext = context.applicationContext
        val savedLang = LangHelper.getSavedLang(appContext)
        _uiState.value = savedLang
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LanguageViewModel()
            }
        }
    }
}
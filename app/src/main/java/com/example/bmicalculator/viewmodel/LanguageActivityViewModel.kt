package com.example.bmicalculator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.util.LangHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LanguageActivityViewModel : ViewModel() {
    private val _selectedLanguage = MutableStateFlow("zh")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
    }
    // 提供加载方法
    fun loadSavedLang(context: Context) {
        val savedLang = LangHelper.getSavedLang(context)
        _selectedLanguage.value = savedLang
    }
    companion object {
        fun provideFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LanguageActivityViewModel()
            }
        }
    }
}
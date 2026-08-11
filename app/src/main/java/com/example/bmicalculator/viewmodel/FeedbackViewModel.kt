package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class FeedbackViewModel : ViewModel() {

    sealed class FeedbackIntent {
        object NavToBack : FeedbackIntent()
        object CommitFeedback : FeedbackIntent()
    }

    fun processIntent(intent: FeedbackIntent) {
        when (intent) {
            is FeedbackIntent.CommitFeedback -> emitEvent(FeedbackEffect.NavToCommit)
            is FeedbackIntent.NavToBack -> emitEvent(FeedbackEffect.NavToBack)
        }
    }

    sealed class FeedbackEffect {
        object NavToBack : FeedbackEffect()
        object NavToCommit : FeedbackEffect()
    }

    private val _effect = MutableSharedFlow<FeedbackEffect>()
    val effect: SharedFlow<FeedbackEffect> = _effect.asSharedFlow()

    private fun emitEvent(newEvent: FeedbackEffect) {
        viewModelScope.launch {
            _effect.emit(newEvent)
        }
    }

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                FeedbackViewModel()
            }
        }
    }
}
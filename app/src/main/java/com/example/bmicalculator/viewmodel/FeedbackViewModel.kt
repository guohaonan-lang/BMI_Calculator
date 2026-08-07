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
            is FeedbackIntent.CommitFeedback -> emitEvent(FeedbackEvent.NavToCommit)
            is FeedbackIntent.NavToBack -> emitEvent(FeedbackEvent.NavToBack)
        }
    }

    sealed class FeedbackEvent {
        object NavToBack : FeedbackEvent()
        object NavToCommit : FeedbackEvent()
    }

    private val _event = MutableSharedFlow<FeedbackEvent>()
    val event: SharedFlow<FeedbackEvent> = _event.asSharedFlow()

    private fun emitEvent(newEvent: FeedbackEvent) {
        viewModelScope.launch {
            _event.emit(newEvent)
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
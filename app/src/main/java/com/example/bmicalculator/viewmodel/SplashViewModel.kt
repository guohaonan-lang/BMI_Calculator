package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(private val repository: BmiRepository) :
    ViewModel() {

    sealed class SplashIntent {
        object NavToInputAct : SplashIntent()
        object NavToInputFra : SplashIntent()
        object CountRecord : SplashIntent()
    }

    fun processIntent(intent: SplashIntent) {
        when (intent) {
            is SplashIntent.NavToInputFra -> emitEffect(SplashEffect.NavInputFraEffect)
            is SplashIntent.NavToInputAct -> emitEffect(SplashEffect.NavInputActEffect)
            is SplashIntent.CountRecord -> countBmiRecord()
        }
    }

    sealed class SplashEffect {
        object NavInputActEffect : SplashEffect()
        object NavInputFraEffect : SplashEffect()
    }

    data class SplashState(
        val recordCount: Long = 0
    )

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect: SharedFlow<SplashEffect> = _effect.asSharedFlow()


    private fun emitEffect(intent: SplashEffect){
        viewModelScope.launch {
            _effect.emit(intent)
        }
    }

    private fun countBmiRecord() {
        viewModelScope.launch {
            _state.update { it.copy(
                recordCount = repository.countBmiRecord()
            )}
        }
    }
    init {
        viewModelScope.launch {
            _state.update { it.copy(
                recordCount = repository.countBmiRecord()
            )}
        }
    }

    companion object {
        fun provideFactory(
            repository: BmiRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SplashViewModel(repository)
            }
        }
    }
}
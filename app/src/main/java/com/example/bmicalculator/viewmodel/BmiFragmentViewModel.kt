package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BmiFragmentViewModel(private val repository: BmiRepository) : ViewModel() {

    private val _bmiRecord: MutableStateFlow<BmiEntity?> = MutableStateFlow(null)
    val bmiRecordFlow: StateFlow<BmiEntity?> = _bmiRecord.asStateFlow()



    // 缓存最新BMI记录，给UI监听
    private val _latestBmiRecord = MutableStateFlow<BmiEntity?>(null)
    val latestBmiRecord: StateFlow<BmiEntity?> = _latestBmiRecord

    init {
        // 全局监听数据库，数据变化自动更新缓存
        viewModelScope.launch {
            repository.getLatestBmi().collect { entity ->
                _latestBmiRecord.value = entity
            }
        }
    }

    companion object {
        fun provideFactory(repository: BmiRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    BmiFragmentViewModel(repository)
                }
            }
    }
}
package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import kotlinx.coroutines.flow.Flow

class BmiViewModel(private val repository: BmiRepository) :
    ViewModel() {

    val allBmiList : Flow<List<BmiEntity>> = repository.getAllBmiRecords()

    suspend fun insertBmiRecord(bmi: BmiEntity) {
        repository.insertBmiRecord(bmi)
    }


    suspend fun updateBmiRecord(bmi: BmiEntity) {
        repository.updateBmiRecord(bmi)
    }

    suspend fun deleteBmiRecord(bmi: BmiEntity) {
        repository.deleteBmiRecord(bmi)
    }

    suspend fun countBmiRecord(): Long{
        return repository.countBmiRecord()
    }

    companion object {
        fun provideFactory(
            repository: BmiRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                BmiViewModel(repository)
            }
        }
    }
}
package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity

class BmiFragmentViewModel(private val repository: BmiRepository) : ViewModel() {


    suspend fun getLatestBmi(): BmiEntity?{
        return repository.getLatestBmi()
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
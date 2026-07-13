package com.example.bmicalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiUtil
import kotlin.Long
import kotlin.math.max
import kotlin.math.min

class ResultViewModel(private val repository: BmiRepository) : ViewModel() {


    var resultBmiRecord = BmiEntity(
    height = 170f,
    heightFt = 5,
    heightIn = 7,
    heightUnit = false,
    weight = 140f,
    weightUnit = false,
    bmiValue = 21.9f,
    bmiColor = 0xFF888888.toInt(), // 临时占位色
    age = 25,
    gender = 1,
    createTime = System.currentTimeMillis(),
    customTime = 0L,
    )
    suspend fun insertBmiRecord(bmi: BmiEntity) {
        repository.insertBmiRecord(bmi)
    }

    suspend fun deleteBmiRecord(bmi: BmiEntity) {
        repository.deleteBmiRecord(bmi)
    }

    suspend fun countBmiRecord(): Long {
        return repository.countBmiRecord()
    }

    data class NormalBmiRange(
        val max: Float,
        val min: Float,
        val difference: Float,
        val sign: String,
        val unit: String
    )
    fun calculatorNormalRange(): NormalBmiRange {
        val unit: String
        var minBmi: Float
        var maxBmi: Float
        if (resultBmiRecord.age <= 20) {
            val teenRange = if (resultBmiRecord.gender == 0) {
                BmiUtil.femaleTeenTable.firstOrNull { it.age == resultBmiRecord.age }
            } else {
                BmiUtil.maleTeenTable.firstOrNull { it.age == resultBmiRecord.age }
            }
            minBmi = teenRange?.underweightMax ?: 0f
            maxBmi = teenRange?.normalMax ?: 0f
        } else {
            minBmi = 18.5f
            maxBmi = 24.9f
        }

        val h: Float = (if (resultBmiRecord.heightUnit) {
            resultBmiRecord.height / 100f
        } else (resultBmiRecord.heightFt * 12f + resultBmiRecord.heightIn) * 2.54f / 100f)


        var minSum = minBmi * h * h
        var maxSum = maxBmi * h * h
        var diff1: Float
        var diff2: Float

        if (resultBmiRecord.weightUnit) {
            diff1 = resultBmiRecord.weight - minSum
            diff2 = resultBmiRecord.weight - maxSum
        } else {
            diff1 = (resultBmiRecord.weight * 0.45359236f) - minSum
            diff2 = (resultBmiRecord.weight * 0.45359236f) - maxSum
        }
        var difference: Float = if (diff1 > 0) min(diff1, diff2)
        else max(diff1, diff2)
        val sign = if (diff1 > 0) "+"
        else ""
        if (resultBmiRecord.weightUnit) {
            unit = "kg"

        } else {
            unit = "lb"
            minSum = minSum / 0.45359236f
            maxSum = maxSum / 0.45359236f
            difference /= 0.45359236f
        }
        return NormalBmiRange(
            max = maxSum,
            min = minSum,
            difference,
            sign,
            unit
        )

    }

    companion object {
        fun provideFactory(repository: BmiRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ResultViewModel(repository)
                }
            }
    }
}
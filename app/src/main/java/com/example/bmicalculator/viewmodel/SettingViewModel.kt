package com.example.bmicalculator.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiFileUtil
import kotlinx.coroutines.launch

class SettingViewModel(private val repository: BmiRepository) : ViewModel() {


    // 导入测试数据
    suspend fun readTestFile(context: Context) {
        val bmiList = BmiFileUtil.readTestFile(context)
        for (item in bmiList) {
            item.id = 0
            val count = repository.getBmiByTime(item.createTime)
            if (count == null) {
                repository.insertBmiRecord(item)
            }
        }
    }

    // 导出数据
    fun exportFile(context: Context, list: List<BmiEntity>) {
        viewModelScope.launch {
            BmiFileUtil.exportBmiToFile(context, list)
        }
    }

    // 导入数据
    fun readFileAndImport(context: Context) {
        viewModelScope.launch {
            val bmiList = BmiFileUtil.readBmiFromFile(context)
            // 清空旧数据
//            repository.clearAll()
            // 写入数据库验证读取结果是否正确
            repository.insertBmiList(bmiList)
        }
    }

    suspend fun getAllList(): List<BmiEntity> {
        return repository.getAllBmiList()
    }

    companion object {
        fun provideFactory(repository: BmiRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingViewModel(repository)
                }
            }
    }
}
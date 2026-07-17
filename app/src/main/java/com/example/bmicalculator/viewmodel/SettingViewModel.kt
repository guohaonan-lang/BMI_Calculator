package com.example.bmicalculator.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiFileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingViewModel(private val repository: BmiRepository) : ViewModel() {



    suspend fun readTestFile(context: Context) {
        withContext(Dispatchers.IO) {
            val bmiList = BmiFileUtil.readTestFile(context)
            for (item in bmiList) {
                item.id = 0
                val count = repository.getBmiByTime(item.createTime)
                // count != null 代表已存在，跳过
                if (count == null) {
                    repository.insertBmiRecord(item)
                }
            }
        }
    }

    fun exportFile(context: Context, list: List<BmiEntity>) {
        viewModelScope.launch {
            BmiFileUtil.exportBmiToFile(context, list)
        }
    }

    fun readFileAndImport(context: Context) {
        viewModelScope.launch {
            // 1.读取文件（核心测试逻辑）
            val bmiList = BmiFileUtil.readBmiFromFile(context)
            // 2.可选：清空旧数据
//            repository.clearAll()
            // 3.写入数据库验证读取结果是否正确
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
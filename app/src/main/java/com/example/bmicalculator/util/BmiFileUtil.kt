package com.example.bmicalculator.util

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.example.bmicalculator.model.BmiEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object BmiFileUtil {
    private val gson = Gson()
    private const val BMI_BACKUP_FILE_NAME = "test_bmi_data.json"
    // assets 内置测试文件名
    private const val ASSETS_TEST_FILE = "test_bmi_data.json"
    // 获取APP私有files目录下的固定文件
    private fun getBmiBackupFile(context: Context): File {
        val filesDir = context.filesDir
        return File(filesDir, BMI_BACKUP_FILE_NAME)
    }

    fun readTestFile(context: Context): List<BmiEntity> {
        return try {
            val inputStream = context.assets.open(ASSETS_TEST_FILE)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonContext = reader.readText()

            val type = object : TypeToken<List<BmiEntity>>(){}
            Log.d(TAG, "读取成功 ： $jsonContext")
            gson.fromJson(jsonContext,type)
        }catch (e: Exception){
            e.printStackTrace()
            Log.d(TAG, "读取失败")
            emptyList()
        }
    }
    // 导出数据
    fun exportBmiToFile(context: Context, recordList: List<BmiEntity>): Boolean {
        return try {
            val targetFile = getBmiBackupFile(context)
            Log.d(TAG, "目标文件路径：${targetFile.absolutePath}")
            // 父目录不存在先创建
            if (!targetFile.parentFile?.exists()!!) {
                targetFile.parentFile?.mkdirs()
            }
            // 创建文件
            if (!targetFile.exists()) {
                val createSuccess = targetFile.createNewFile()
                Log.d(TAG, "新建文件结果：$createSuccess")
            }
            // append=false 覆盖写入
            FileOutputStream(targetFile).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    val json = gson.toJson(recordList)
                    writer.write(json)
                    writer.flush()
                }
            }
            Log.d(TAG, "写入完成，文件大小：${targetFile.length()}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "写入文件失败", e)
            false
        }
    }

    // 导入数据
    fun readBmiFromFile(context: Context): List<BmiEntity> {

        val targetFile = getBmiBackupFile(context)
        if (!targetFile.exists() || targetFile.length() == 0L) return emptyList()

        FileReader(targetFile).use { reader ->
            val type = object : TypeToken<List<BmiEntity>>() {}
            return gson.fromJson(reader, type)
        }
    }
    // 删除本地备份文件
    fun deleteLocalBackupFile(context: Context): Boolean {
        return getBmiBackupFile(context).delete()
    }

    // 获取文件绝对路径
    fun getLocalBackupFilePath(context: Context): String {
        return getBmiBackupFile(context).absolutePath
    }
}
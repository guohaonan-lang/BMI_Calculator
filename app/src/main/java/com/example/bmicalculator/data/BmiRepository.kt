package com.example.bmicalculator.data

import com.example.bmicalculator.model.BmiEntity
import kotlinx.coroutines.flow.Flow

class BmiRepository(private val bmiDao: BmiDao) {
    fun getAllBmiRecords(): Flow<List<BmiEntity>> {
        return bmiDao.getAllBmi()
    }

    fun getChartBmi(): Flow<List<BmiEntity>> {
        return bmiDao.getChartBmi()
    }

    suspend fun getAllBmiList(): List<BmiEntity> {
        return bmiDao.getAllBmiList()
    }

    suspend fun getBmiByTime(time: Long): BmiEntity? {
        return bmiDao.getBmiByTime(time)
    }

    fun getLatestBmi(): Flow<BmiEntity?> {
        return bmiDao.getLatestBmiOnce()
    }

    suspend fun insertBmiRecord(bmi: BmiEntity) {
        bmiDao.insertBmi(bmi)
    }

    suspend fun insertBmiList(list: List<BmiEntity>) {
        bmiDao.insertBmiList(list)
    }

    suspend fun updateBmiRecord(bmi: BmiEntity) {
        bmiDao.updatebmi(bmi)
    }

    suspend fun deleteBmiRecord(bmi: BmiEntity) {
        bmiDao.deletebmi(bmi)
    }
    suspend fun countBmiRecord(): Long{
        return bmiDao.countAllBmi()
    }
}
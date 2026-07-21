package com.example.bmicalculator.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bmicalculator.model.BmiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BmiDao {
    @Query("SELECT * FROM bmi  ORDER BY customTime Desc,createTime Desc")
    fun getAllBmi(): Flow<List<BmiEntity>>

    @Query("SELECT * FROM bmi  ORDER BY customTime Asc,createTime Asc")
    fun getChartBmi(): Flow<List<BmiEntity>>

    @Query("SELECT * FROM bmi ORDER BY customTime DESC LIMIT 1")
    fun getLatestBmiOnce(): Flow<BmiEntity?>

    @Query("SELECT * FROM bmi ORDER BY customTime DESC")
    suspend fun getAllBmiList(): List<BmiEntity>

    @Query("SELECT COUNT(*) FROM bmi")
    suspend fun countAllBmi(): Long

    @Query("SELECT * FROM bmi WHERE createTime = :time")
    suspend fun getBmiByTime(time : Long): BmiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBmi(bmi: BmiEntity): Long

    @Insert
    suspend fun insertBmiList(list: List<BmiEntity>)

    @Update
    suspend fun updateBmi(bmi: BmiEntity)

    @Delete
    suspend fun deleteBmi(bmi: BmiEntity)
}
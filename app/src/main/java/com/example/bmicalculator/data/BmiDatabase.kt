package com.example.bmicalculator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bmicalculator.R
import com.example.bmicalculator.model.BmiEntity


@Database(
    entities = [BmiEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BmiDatabase : RoomDatabase() {
    abstract fun bmiDao(): BmiDao

    companion object {
        @Volatile
        private var INSTANCE: BmiDatabase? = null
        fun getDatabase(context: Context): BmiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BmiDatabase::class.java,
                    context.getString(R.string.database_name)
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
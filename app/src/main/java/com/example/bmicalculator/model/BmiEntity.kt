package com.example.bmicalculator.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("bmi")
data class BmiEntity(
    @PrimaryKey(true) val id: Long,
    // 身高 cm
    var height: Float,
    // 体重 kg
    var weight: Float,
    // BMI 计算结果
    var bmiValue: Float,
    // 年龄
    var age: Int,
    // 性别 0女 / 1男
    var gender: Int,

    // 记录创建时间（系统自动时间戳）
    var createTime: Long,
    // 自定义记录时间（用户手动选择的日期时间戳）
    var customTime: Long
)

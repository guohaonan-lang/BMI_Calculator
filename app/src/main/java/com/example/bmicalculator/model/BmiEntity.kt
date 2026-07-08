package com.example.bmicalculator.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity("bmi")
@Parcelize
data class BmiEntity(
    @PrimaryKey(true) var id: Long = 0,
    var weight: Float,
    var weightUnit: Boolean = false,

    var height: Float,
    var heightFt: Int,
    var heightIn: Int,
    var heightUnit: Boolean = false,

    // BMI 计算结果
    var bmiValue: Float,
    var bmiGrade: String,//等级名称
    var bmiColor: Int,//对应颜色
    // 年龄
    var age: Int,
    // 性别 0女 / 1男
    var gender: Int,

    // 记录创建时间（系统自动时间戳）
    var createTime: Long = System.currentTimeMillis(),
    // 自定义记录时间（用户手动选择的日期时间戳）
    var customTime: Long,
    var timeText: String
) : Parcelable

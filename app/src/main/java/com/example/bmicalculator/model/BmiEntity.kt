package com.example.bmicalculator.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bmicalculator.R
import kotlinx.parcelize.Parcelize


@Entity("bmi")
@Parcelize
data class BmiEntity(
    @PrimaryKey(true) var id: Long = 0,
    var weight: Float = 140f,
    var weightUnit: Boolean = false,

    var height: Float = 170f,
    var heightFt: Int = 5,
    var heightIn: Int = 7,
    var heightUnit: Boolean = false,

    // BMI 计算结果
    var bmiValue: Float = 21.9f,
    var bmiColor: Int = 0,//对应颜色
    // 年龄
    var age: Int = 25,
    // 性别 0女 / 1男
    var gender: Int = 1,

    // 记录创建时间（系统自动时间戳）
    var createTime: Long = System.currentTimeMillis(),
    // 自定义记录时间（用户手动选择的日期时间戳）
    var customTime: Long = 0,
) : Parcelable

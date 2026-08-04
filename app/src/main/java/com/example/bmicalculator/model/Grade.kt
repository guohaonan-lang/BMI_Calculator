package com.example.bmicalculator.model

data class Grade(
    val color: Int,
    val gradeNameInt: Int,
    val gradeRangeInt: Int = 0,
    val gradeRange: String = "",
    var isSelect: Boolean = false
)

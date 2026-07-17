package com.example.bmicalculator.model

data class Grade(
    val color: Int,
    val gradeName: String,
    val gradeRange: String,
    var isSelect: Boolean = false
)

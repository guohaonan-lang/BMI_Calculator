package com.example.bmicalculator.model

data class BmiLevel(
    val levelName: String,
    val colorInt: Int
)

data class TeenBmiRange(
    val age: Int,
    val underweightMax: Float,    // < 该值=偏瘦 Underweight
    val normalMax: Float,         // under~normalMax 正常
    val overweightMax: Float      // normalMax ~ overweightMax 超重，>=则肥胖I
)
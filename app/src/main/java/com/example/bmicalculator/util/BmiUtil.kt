package com.example.bmicalculator.util

import com.example.bmicalculator.R
import com.example.bmicalculator.model.BmiLevel
import com.example.bmicalculator.model.TeenBmiRange

object BmiUtil {
    // 配色池（和你之前完全一致）
    private val colorMap = mapOf(
        "VERY_SEVERELY_UNDER" to R.color.grad1,
        "SEVERELY_UNDER" to R.color.grad2,
        "UNDER" to R.color.grad3,
        "NORMAL" to R.color.grad4,
        "OVER" to R.color.grad5,
        "OBESE1" to R.color.grad6,
        "OBESE2" to R.color.grad7,
        "OBESE3" to R.color.grad8
    )

    // region 成年标准阈值（>=18岁）
    private const val ADULT_VSU_MAX = 16.0f
    private const val ADULT_SU_MAX = 16.9f
    private const val ADULT_U_MAX = 18.4f
    private const val ADULT_N_MAX = 24.9f
    private const val ADULT_O_MAX = 29.9f
    private const val ADULT_OB1_MAX = 34.9f
    private const val ADULT_OB2_MAX = 39.9f
    // endregion

    // region 未成年女生 2~20岁 阈值表
    val femaleTeenTable = listOf(
        TeenBmiRange(2, 14.4f, 17.9f, 19.0f),
        TeenBmiRange(3, 14.0f, 17.1f, 18.2f),
        TeenBmiRange(4, 13.7f, 16.7f, 17.9f),
        TeenBmiRange(5, 13.5f, 16.7f, 18.2f),
        TeenBmiRange(6, 13.4f, 17.1f, 18.7f),
        TeenBmiRange(7, 13.5f, 17.5f, 19.5f),
        TeenBmiRange(8, 13.6f, 18.3f, 20.5f),
        TeenBmiRange(9, 13.8f, 19.1f, 21.7f),
        TeenBmiRange(10, 14.0f, 19.9f, 22.9f),
        TeenBmiRange(11, 14.8f, 21.6f, 25.1f),
        TeenBmiRange(12, 14.8f, 21.6f, 25.1f),
        TeenBmiRange(13, 15.4f, 22.5f, 26.3f),
        TeenBmiRange(14, 15.8f, 23.3f, 27.1f),
        TeenBmiRange(15, 16.4f, 24.0f, 28.0f),
        TeenBmiRange(16, 16.8f, 24.5f, 28.8f),
        TeenBmiRange(17, 17.2f, 25.1f, 29.5f),
        TeenBmiRange(18, 17.6f, 25.5f, 30.3f),
        TeenBmiRange(19, 17.8f, 26.1f, 30.9f),
        TeenBmiRange(20, 17.9f, 26.4f, 31.6f)
    )
    // endregion

    // region 未成年男生 2~20岁 阈值表
    val maleTeenTable = listOf(
        TeenBmiRange(2, 14.8f, 18.1f, 19.2f),
        TeenBmiRange(3, 14.4f, 17.3f, 18.2f),
        TeenBmiRange(4, 14.0f, 16.8f, 17.9f),
        TeenBmiRange(5, 13.8f, 16.7f, 18.0f),
        TeenBmiRange(6, 13.7f, 16.9f, 18.5f),
        TeenBmiRange(7, 13.6f, 17.3f, 19.1f),
        TeenBmiRange(8, 13.7f, 17.7f, 19.9f),
        TeenBmiRange(9, 14.0f, 18.5f, 21.0f),
        TeenBmiRange(10, 14.2f, 19.3f, 22.1f),
        TeenBmiRange(11, 14.5f, 19.9f, 23.1f),
        TeenBmiRange(12, 15.0f, 20.9f, 24.1f),
        TeenBmiRange(13, 15.5f, 21.7f, 25.3f),
        TeenBmiRange(14, 16.0f, 22.5f, 25.9f),
        TeenBmiRange(15, 16.5f, 23.4f, 26.7f),
        TeenBmiRange(16, 17.1f, 24.1f, 27.6f),
        TeenBmiRange(17, 17.6f, 24.7f, 28.2f),
        TeenBmiRange(18, 18.3f, 25.5f, 28.9f),
        TeenBmiRange(19, 18.5f, 26.3f, 29.7f),
        TeenBmiRange(20, 18.5f, 27.1f, 30.6f)
    )
    // endregion

    /**
     * 统一入口
     * @param age 年龄
     * @param gender 0女 / 1男
     * @param bmiValue BMI数值
     * @return 等级文字+颜色
     */
    fun getBmiFullInfo(age: Int, gender: Int, bmiValue: Float): BmiLevel {
        // 成年人标准 >=18
        if (age >= 18) {
            return getAdultBmi(bmiValue)
        }
        // 未成年 2~20
        if (age in 2..20) {
            val table = if (gender == 0) femaleTeenTable else maleTeenTable
            val range = table.first { it.age == age }
            return getTeenBmi(bmiValue, range)
        }
        // 小于2岁兜底，按成人正常区间返回
        return BmiLevel("Normal", colorMap["NORMAL"]!!)
    }

    // 成年人8档判断
    private fun getAdultBmi(bmi: Float): BmiLevel {
        return when {
            bmi < ADULT_VSU_MAX -> BmiLevel(
                "Very Severely Underweight",
                colorMap["VERY_SEVERELY_UNDER"]!!
            )

            bmi <= ADULT_SU_MAX -> BmiLevel("Severely Underweight", colorMap["SEVERELY_UNDER"]!!)
            bmi <= ADULT_U_MAX -> BmiLevel("Underweight", colorMap["UNDER"]!!)
            bmi <= ADULT_N_MAX -> BmiLevel("Normal", colorMap["NORMAL"]!!)
            bmi <= ADULT_O_MAX -> BmiLevel("Overweight", colorMap["OVER"]!!)
            bmi <= ADULT_OB1_MAX -> BmiLevel("Obese Class I", colorMap["OBESE1"]!!)
            bmi <= ADULT_OB2_MAX -> BmiLevel("Obese Class II", colorMap["OBESE2"]!!)
            else -> BmiLevel("Obese Class III", colorMap["OBESE3"]!!)
        }
    }

    // 未成年4档：Underweight / Normal / Overweight / Obese Class I
    private fun getTeenBmi(bmi: Float, r: TeenBmiRange): BmiLevel {
        return when {
            bmi < r.underweightMax -> BmiLevel("Underweight", colorMap["UNDER"]!!)
            bmi <= r.normalMax -> BmiLevel("Normal", colorMap["NORMAL"]!!)
            bmi <= r.overweightMax -> BmiLevel("Overweight", colorMap["OVER"]!!)
            else -> BmiLevel("Obese Class I", colorMap["OBESE1"]!!)
        }
    }
}
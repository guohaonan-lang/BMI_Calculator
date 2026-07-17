package com.example.bmicalculator.util

import android.content.Context
import com.example.bmicalculator.R
import com.example.bmicalculator.model.BmiLevel
import com.example.bmicalculator.model.Grade
import com.example.bmicalculator.model.TeenBmiRange

object BmiUtil {
    // 配色池（资源ID，无需上下文）
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
     * @param context 上下文（Activity/Fragment传入this/requireContext）
     * @param age 年龄
     * @param gender 0女 / 1男
     * @param bmiValue BMI数值
     * @return 等级文字+颜色+多语言评价
     */
    fun getBmiFullInfo(context: Context, age: Int, gender: Int, bmiValue: Float): BmiLevel {
        // 成年人标准 >=18
        if (age >= 18) {
            return getAdultBmi(context, bmiValue)
        }
        // 未成年 2~20
        if (age in 2..20) {
            val table = if (gender == 0) femaleTeenTable else maleTeenTable
            val range = table.first { it.age == age }
            return getTeenBmi(context, bmiValue, range)
        }
        // 小于2岁兜底，正常区间
        val name = context.getString(R.string.adults_bmi_normal)
        val desc = context.getString(R.string.desc_n)
        return BmiLevel(name, colorMap["NORMAL"]!!, desc)
    }

    // 成年人8档判断
    private fun getAdultBmi(context: Context, bmi: Float): BmiLevel {
        return when {
            bmi < ADULT_VSU_MAX -> {
                val name = context.getString(R.string.adults_bmi_very_severely_underweight)
                val desc = context.getString(R.string.desc_vsu)
                BmiLevel(name, colorMap["VERY_SEVERELY_UNDER"]!!, desc)
            }

            bmi <= ADULT_SU_MAX -> {
                val name = context.getString(R.string.adults_bmi_severely_underweight)
                val desc = context.getString(R.string.desc_su)
                BmiLevel(name, colorMap["SEVERELY_UNDER"]!!, desc)
            }

            bmi <= ADULT_U_MAX -> {
                val name = context.getString(R.string.adults_bmi_underweight)
                val desc = context.getString(R.string.desc_u)
                BmiLevel(name, colorMap["UNDER"]!!, desc)
            }

            bmi <= ADULT_N_MAX -> {
                val name = context.getString(R.string.adults_bmi_normal)
                val desc = context.getString(R.string.desc_n)
                BmiLevel(name, colorMap["NORMAL"]!!, desc)
            }

            bmi <= ADULT_O_MAX -> {
                val name = context.getString(R.string.adults_bmi_overweight)
                val desc = context.getString(R.string.desc_o)
                BmiLevel(name, colorMap["OVER"]!!, desc)
            }

            bmi <= ADULT_OB1_MAX -> {
                val name = context.getString(R.string.adults_bmi_obese_class_i)
                val desc = context.getString(R.string.desc_ob1)
                BmiLevel(name, colorMap["OBESE1"]!!, desc)
            }

            bmi <= ADULT_OB2_MAX -> {
                val name = context.getString(R.string.adults_bmi_obese_class_ii)
                val desc = context.getString(R.string.desc_ob2)
                BmiLevel(name, colorMap["OBESE2"]!!, desc)
            }

            else -> {
                val name = context.getString(R.string.adults_bmi_obese_class_iii)
                val desc = context.getString(R.string.desc_ob3)
                BmiLevel(name, colorMap["OBESE3"]!!, desc)
            }
        }
    }

    // 未成年4档：Underweight / Normal / Overweight / Obese Class I
    private fun getTeenBmi(context: Context, bmi: Float, r: TeenBmiRange): BmiLevel {
        return when {
            bmi < r.underweightMax -> {
                val name = context.getString(R.string.adults_bmi_underweight)
                val desc = context.getString(R.string.desc_teen_u)
                BmiLevel(name, colorMap["UNDER"]!!, desc)
            }

            bmi <= r.normalMax -> {
                val name = context.getString(R.string.adults_bmi_normal)
                val desc = context.getString(R.string.desc_teen_n)
                BmiLevel(name, colorMap["NORMAL"]!!, desc)
            }

            bmi <= r.overweightMax -> {
                val name = context.getString(R.string.adults_bmi_overweight)
                val desc = context.getString(R.string.desc_teen_o)
                BmiLevel(name, colorMap["OVER"]!!, desc)
            }

            else -> {
                val name = context.getString(R.string.adults_bmi_obese_class_i)
                val desc = context.getString(R.string.desc_teen_ob1)
                BmiLevel(name, colorMap["OBESE1"]!!, desc)
            }
        }
    }

    fun getGradeIndex(context: Context, levelName: String): Int {
        val strVerySevere = context.getString(R.string.adults_bmi_very_severely_underweight)
        val strSevere = context.getString(R.string.adults_bmi_severely_underweight)
        val strUnder = context.getString(R.string.adults_bmi_underweight)
        val strNormal = context.getString(R.string.adults_bmi_normal)
        val strOver = context.getString(R.string.adults_bmi_overweight)
        val strOb1 = context.getString(R.string.adults_bmi_obese_class_i)
        val strOb2 = context.getString(R.string.adults_bmi_obese_class_ii)
        val strOb3 = context.getString(R.string.adults_bmi_obese_class_iii)
        var grad = 0
        when (levelName) {
            strVerySevere -> grad = 1
            strSevere -> grad = 2
            strUnder -> grad = 3
            strNormal -> grad = 4
            strOver -> grad = 5
            strOb1 -> grad = 6
            strOb2 -> grad = 7
            strOb3 -> grad = 8
        }
        return grad
    }

    fun getTeenBmiRange(age: Int, gender: Int): FloatArray {

        val teenRange = if (gender == 0) {
            femaleTeenTable.firstOrNull { it.age == age }
        } else {
            maleTeenTable.firstOrNull { it.age == age }
        }
        val minBmi = teenRange?.underweightMax?.minus(1f) ?: 13f
        val maxBmi = teenRange?.overweightMax?.plus(1f) ?: 33f
        val bmiRanges = if (teenRange != null) {
            floatArrayOf(
                minBmi,
                teenRange.underweightMax,
                teenRange.normalMax,
                teenRange.overweightMax,
                maxBmi
            )
        } else {
            floatArrayOf(13f, 15f, 20f, 25f, 33f)
        }
        return bmiRanges
    }

    fun getGradeList(context: Context, age: Int, gender: Int): List<Grade> {
        if (age <= 20) return getTeenBmiColorAndRange(context, age, gender)
        else return getAdultBmiColorAndRange(context)
    }

    private fun getAdultBmiColorAndRange(context: Context): List<Grade> {
        val gradeList = listOf(
            Grade(
                context.getColor(R.color.grad1),
                context.getString(R.string.adults_bmi_very_severely_underweight),
                context.getString(R.string.adults_bmi_range_VerySeverelyUnderweight)
            ),
            Grade(
                context.getColor(R.color.grad2),
                context.getString(R.string.adults_bmi_severely_underweight),
                context.getString(R.string.adults_bmi_range_SeverelyUnderweight)
            ),
            Grade(
                context.getColor(R.color.grad3),
                context.getString(R.string.adults_bmi_underweight),
                context.getString(R.string.adults_bmi_range_overweight)
            ),
            Grade(
                context.getColor(R.color.grad4),
                context.getString(R.string.adults_bmi_normal),
                context.getString(R.string.adults_bmi_range_normal)
            ),
            Grade(
                context.getColor(R.color.grad5),
                context.getString(R.string.adults_bmi_overweight),
                context.getString(R.string.adults_bmi_range_overweight)
            ),
            Grade(
                context.getColor(R.color.grad6),
                context.getString(R.string.adults_bmi_obese_class_i),
                context.getString(R.string.adults_bmi_range_obese_class_i)
            ),
            Grade(
                context.getColor(R.color.grad7),
                context.getString(R.string.adults_bmi_obese_class_ii),
                context.getString(R.string.adults_bmi_range_obese_class_ii)
            ),
            Grade(
                context.getColor(R.color.grad8),
                context.getString(R.string.adults_bmi_obese_class_iii),
                context.getString(R.string.adults_bmi_range_obese_class_iii)
            )
        )
        return gradeList
    }

    private fun getTeenBmiColorAndRange(context: Context, age: Int, gender: Int): List<Grade> {
        val teenRange = if (gender == 0) {
            femaleTeenTable.firstOrNull { it.age == age }
        } else {
            maleTeenTable.firstOrNull { it.age == age }
        }
        val gradeList = listOf(
            Grade(
                context.getColor(R.color.grad3),
                context.getString(R.string.adults_bmi_underweight),
                " < ${teenRange?.underweightMax}"
            ),
            Grade(
                context.getColor(R.color.grad4),
                context.getString(R.string.adults_bmi_normal),
                "${teenRange?.underweightMax}- ${teenRange?.normalMax}"
            ),
            Grade(
                context.getColor(R.color.grad5),
                context.getString(R.string.adults_bmi_overweight),
                "${teenRange?.normalMax}- ${teenRange?.overweightMax}"
            ),
            Grade(
                context.getColor(R.color.grad6),
                context.getString(R.string.adults_bmi_obese_class_i),
                " ≥ ${teenRange?.overweightMax}"
            )
        )
        return gradeList
    }

}
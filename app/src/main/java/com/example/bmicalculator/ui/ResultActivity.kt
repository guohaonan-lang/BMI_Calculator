package com.example.bmicalculator.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivityResultBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.util.BmiColorWheelView
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil
import com.example.bmicalculator.viewmodel.BmiViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var sheetDialog: BottomSheetDialog
    private lateinit var alertDialog: AlertDialog

    private val viewModel: BmiViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        BmiViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    private var bmiRecord: BmiEntity? = null
    private var statusFirst: Boolean = false
    private var statusRecent: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initData()

        initBottomDialog()
        initDeleteDialog()

        //判断不同的页面，控制部分控件显隐
        initChangePage()


//        binding.resultAssessment.text2 = "Normal Weight for your height (180cm):"
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun initData() {
        // 1.读取数据
        bmiRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("BMI", BmiEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("BMI")
        }
        statusFirst = intent.getBooleanExtra("FATHER", false)
        statusRecent = intent.getBooleanExtra("Recent", false)

        // 2. 非空校验，渲染数据
        bmiRecord?.let { record ->
            val targetBmi = record.bmiValue
            val anim = ValueAnimator.ofFloat(0f, targetBmi)
            anim.duration = 1000 // 动画时长1.2秒，可自行修改
            anim.addUpdateListener { animation ->
                val current = animation.animatedValue as Float
                binding.resultMergeResult.mergeResultBmi.text = String.format("%.1f", current)
            }
            anim.start()


            binding.resultMergeResult.mergeBmiGauge.currentBmi = record.bmiValue

            if (record.weightUnit) binding.resultMergeResult.mergeResultWeight.text =
                "${record.weight} kg"
            else binding.resultMergeResult.mergeResultWeight.text = "${record.weight} lb"
            if (record.heightUnit) binding.resultMergeResult.mergeResultHeight.text =
                "${record.height} cm"
            else binding.resultMergeResult.mergeResultHeight.text =
                "${record.heightFt}ft ${record.heightIn}in"

            binding.resultMergeResult.mergeResultAge.text = record.age.toString()
            binding.resultMergeResult.mergeResultGender.text =
                if (record.gender == 1) getString(R.string.male) else getString(R.string.female)


            val wheel = binding.resultMergeResult.mergeBmiGauge
            wheel.age = record.age
            wheel.gender = record.gender
            wheel.currentBmi = record.bmiValue

            //  评论
            val bmiInfo = BmiUtil.getBmiFullInfo(this, record.age, record.gender, record.bmiValue)
            binding.resultMergeResult.mergeResultGrade.text = bmiInfo.levelName
            binding.resultMergeResult.mergeResultGrade.backgroundTintList =
                ColorStateList.valueOf(record.bmiColor)
            binding.assessmentText1.text = bmiInfo.assessment
            if (bmiInfo.levelName == getString(R.string.adults_bmi_normal)) {
                binding.assessmentText2.visibility = View.GONE
                binding.assessmentRange.visibility = View.GONE
                binding.assessmentDifference.visibility = View.GONE
            } else {
                val baseText = getString(R.string.result_assessment_weight)
                if (record.heightUnit) binding.assessmentText2.text =
                    "$baseText ${record.height} cm"
                else binding.assessmentText2.text =
                    "$baseText (${record.heightFt}ft ${record.heightIn}in):"


                var minBmi: Float
                var maxBmi: Float
                if (record.age <= 20) {
                    val teenRange = if (record.gender == 0) {
                        BmiUtil.femaleTeenTable.firstOrNull { it.age == record.age }
                    } else {
                        BmiUtil.maleTeenTable.firstOrNull { it.age == record.age }
                    }
                    minBmi = teenRange?.underweightMax ?: 0f
                    maxBmi = teenRange?.normalMax ?: 0f
                } else {
                    minBmi = 18.5f
                    maxBmi = 24.9f
                }


                val h: Float = (if (record.heightUnit) {
                    record.height / 100f
                } else (record.heightFt * 12f + record.heightIn) * 2.54f / 100f)


                val minkg = minBmi * h * h
                val maxkg = maxBmi * h * h
                var diff1: Float
                var diff2: Float

                if (record.weightUnit) {
                    diff1 = record.weight - minkg
                    diff2 = record.weight - maxkg
                } else {
                    diff1 = (record.weight * 0.45359236f) - minkg
                    diff2 = (record.weight * 0.45359236f) - maxkg
                }
                var difference: Float = if (diff1 > 0) min(diff1, diff2)
                else max(diff1, diff2)
                if (record.weightUnit) {
                    binding.assessmentRange.text = "%.1f kg - %.1f kg".format(minkg, maxkg)
                    if (diff1 > 0) binding.assessmentDifference.text =
                        "(+%.1f kg)".format(difference)
                    else binding.assessmentDifference.text = "(%.1f kg)".format(difference)
                } else {
                    val minlb = minkg / 0.45359236f
                    val maxlb = maxkg / 0.45359236f
                    difference /= 0.45359236f

                    Toast.makeText(this, "$diff1  $diff2", Toast.LENGTH_SHORT).show()
                    binding.assessmentRange.text = "%.1f lb - %.1f lb".format(minlb, maxlb)
                    if (diff1 > 0) binding.assessmentDifference.text =
                        "(+%.1f lb)".format(difference)
                    else binding.assessmentDifference.text = "(%.1f lb)".format(difference)
                }

            }

            setupBmiGard(bmiInfo.levelName)

        } ?: run {
            // 无数据返回输入页
            Toast.makeText(
                this,
                getString(R.string.result_activity_data_transmission_failed), Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun initChangePage() {

        val timetext = TimeUtil(this).parseTimeStamp(bmiRecord?.customTime ?: 0)
        val text =
            "${timetext.selectMonth} ${timetext.selectDay} ${timetext.selectYear}  ${timetext.selectPeriod}"
        binding.resultMergeAd.tvTimeTag.text = text
        if (statusRecent) {
            // 历史结果图
            binding.resultMergeGrade.root.visibility = View.GONE
            binding.resultSave.visibility = View.GONE
            binding.resultDelete.visibility = View.GONE
            binding.resultRecentDelete.visibility = View.VISIBLE
            binding.resultRecentBack.visibility = View.VISIBLE

            binding.resultRecentBack.setOnClickListener {
                finish()
            }
            binding.resultRecentDelete.setOnClickListener {
                alertDialog.show()
            }
            val btn = binding.resultMergeResult.mergeResultGrade
            btn.setOnClickListener {
                sheetDialog.show()
            }
            // 参数：start, top, end, bottom 资源ID
            btn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.help_circle, // 右侧图标
                0
            )
            // 图标和文字间距
            btn.compoundDrawablePadding = 10

        } else {

            binding.resultDelete.setOnClickListener {
                alertDialog.show()
            }

            onBackPressedDispatcher.addCallback(this, onBackPressedCallback)


            binding.resultSave.setOnClickListener {
                lifecycleScope.launch {
                    val newRecord = bmiRecord!!.copy(id = 0) // 清空主键，生成全新记录
                    viewModel.insertBmiRecord(newRecord)
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

            if (statusFirst) {
                binding.resultMergeAd.root.visibility = View.GONE

            } else {
                val btn = binding.resultMergeResult.mergeResultGrade
                // 参数：start, top, end, bottom 资源ID
                btn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.help_circle, // 右侧图标
                    0
                )
                btn.setOnClickListener {
                    sheetDialog.show()
                }
                // 图标和文字间距
                btn.compoundDrawablePadding = 10
                binding.resultMergeGrade.root.visibility = View.GONE
                binding.resultMergeAd.tvTimeTag.visibility = View.GONE

            }
        }
    }

    private fun setupBmiGard(levelName: String) {
        var bmiRanges: FloatArray

        val strVerySevere = getString(R.string.adults_bmi_very_severely_underweight)
        val strSevere = getString(R.string.adults_bmi_severely_underweight)
        val strUnder = getString(R.string.adults_bmi_underweight)
        val strNormal = getString(R.string.adults_bmi_normal)
        val strOver = getString(R.string.adults_bmi_overweight)
        val strOb1 = getString(R.string.adult_bmi_obese_class_i)
        val strOb2 = getString(R.string.adults_bmi_obese_class_ii)
        val strOb3 = getString(R.string.adults_bmi_obese_class_iii)
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
        highlightGradeItem(grad)
        bmiRecord?.let { record ->

            if (record.age <= 20) {
                val teenRange = if (record.gender == 0) {
                    BmiUtil.femaleTeenTable.firstOrNull { it.age == record.age }
                } else {
                    BmiUtil.maleTeenTable.firstOrNull { it.age == record.age }
                }
                val minBmi = teenRange?.underweightMax?.minus(1f) ?: 13f
                val maxBmi = teenRange?.overweightMax?.plus(1f) ?: 33f
                bmiRanges = if (teenRange != null) {
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
                switchTeenGrade(bmiRanges)

            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun switchTeenGrade(bmiRanges: FloatArray) {
        val rootLayout = binding.resultMergeGrade.root
        val ctx = rootLayout.context

        // 重置全部条目
        for (i in 1..8) {
            val resetId = ctx.resources.getIdentifier("merge_grade_list_$i", "id", ctx.packageName)
            val resetItem = rootLayout.findViewById<ViewGroup>(resetId)
            val scopeId =
                ctx.resources.getIdentifier("merge_grade_list_scope_$i", "id", ctx.packageName)
            val scopeTv = resetItem.findViewById<TextView>(scopeId)
            if (i in 3..6) {
                when (i) {
                    3 -> {
                        scopeTv.text = " < ${bmiRanges[0]}"
                    }

                    6 -> {
                        val s = getString(R.string.adult_bmi_range_obese_class_iii)
                        scopeTv.text = "${s[0]} ${bmiRanges.last()}"
                    }

                    else -> {

                        scopeTv.text = "${bmiRanges[i - 4]} - ${bmiRanges[i - 3]}"
                    }
                }

            } else {
                resetItem.visibility = View.GONE
            }

        }
    }

    private fun highlightGradeItem(grad: Int) {
        val rootLayout = binding.resultMergeGrade.root
        val ctx = rootLayout.context
        val white = ctx.getColor(android.R.color.white)

        // 1. 获取当前条目根ConstraintLayout
        val itemId = ctx.resources.getIdentifier("merge_grade_list_$grad", "id", ctx.packageName)
        val targetItem =
            rootLayout.findViewById<ConstraintLayout>(itemId)

        // 2. 获取内部三个子控件
        val colorViewId =
            ctx.resources.getIdentifier("merge_grade_list_color_$grad", "id", ctx.packageName)
        val textId =
            ctx.resources.getIdentifier("merge_grade_list_text_$grad", "id", ctx.packageName)
        val scopeId =
            ctx.resources.getIdentifier("merge_grade_list_scope_$grad", "id", ctx.packageName)

        val colorView = targetItem.findViewById<View>(colorViewId)
        val textTv = targetItem.findViewById<TextView>(textId)
        val scopeTv = targetItem.findViewById<TextView>(scopeId)

        // 3. 设置根布局backgroundTint
        val colorResId = ctx.resources.getIdentifier("grad$grad", "color", ctx.packageName)
        val tintColor = ctx.getColor(colorResId)
        ViewCompat.setBackgroundTintList(targetItem, ColorStateList.valueOf(tintColor))

        // 4. 子控件背景白色
        ViewCompat.setBackgroundTintList(colorView, ColorStateList.valueOf(white))
        textTv.setTextColor(white)
        scopeTv.setTextColor(white)
        val font = ResourcesCompat.getFont(this, R.font.font_extrabold)
        textTv.typeface = font
        scopeTv.typeface = font
        textTv.alpha = 1f
        scopeTv.alpha = 1f
    }


    // 初始化delete弹窗
    private fun initDeleteDialog() {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_delete, null)

        alertDialog = AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setCancelable(true)
            .create()

        val buttonYes = dialogLayout.findViewById<TextView>(R.id.delete)
        val buttonNo = dialogLayout.findViewById<TextView>(R.id.cancel)
        buttonNo.setOnClickListener { alertDialog.dismiss() }
        buttonYes.setOnClickListener {
            var sum: Long = 1
            lifecycleScope.launch {
                sum = viewModel.countBmiRecord()
                if (statusRecent) viewModel.deleteBmiRecord(bmiRecord!!)
            }
            if (statusRecent && sum.toInt() == 0) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            } else {
                alertDialog.dismiss()
                finish()
            }

        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    // 初始化bottomDialog
    @SuppressLint("InflateParams")
    private fun initBottomDialog() {
        sheetDialog = BottomSheetDialog(this)
        val rootView =
            LayoutInflater.from(this).inflate(R.layout.bottom_sheet_grade, null)
        sheetDialog.setContentView(rootView)
        rootView.findViewById<Button>(R.id.dialog_got).setOnClickListener {
            sheetDialog.dismiss()
        }
        val wheel = rootView.findViewById<BmiColorWheelView>(R.id.dialog_bmi_wheel)
        bmiRecord?.let { record ->
            wheel.age = record.age
            wheel.gender = record.gender
            wheel.currentBmi = record.bmiValue
        }

        //        sheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

    }

    //返回监听，触发delete弹窗
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            alertDialog.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销返回键监听
        onBackPressedCallback.isEnabled = false
    }
}
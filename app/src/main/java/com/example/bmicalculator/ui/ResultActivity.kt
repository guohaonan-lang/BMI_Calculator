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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.GradeAdapter
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivityResultBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.model.Grade
import com.example.bmicalculator.util.BmiColorWheelView
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil
import com.example.bmicalculator.viewmodel.ResultViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var sheetDialog: BottomSheetDialog
    private lateinit var gradeAdapter: GradeAdapter
    private lateinit var alertDialog: AlertDialog

    private val viewModel: ResultViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        ResultViewModel.provideFactory(BmiRepository(db.bmiDao()))
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

        initGradeRecyclerView()

        initData()
        initBottomDialog()
        initDeleteDialog()
        //判断不同的页面，控制部分控件显隐
        initChangePage()
        initDataFlow()
    }

    private fun initGradeRecyclerView() {
        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.resultGradeRv.layoutManager = layoutManager
        gradeAdapter = GradeAdapter(emptyList())
        binding.resultGradeRv.adapter = gradeAdapter
    }

    private fun initDataFlow() {

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
            viewModel.resultBmiRecord = record
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


                val normalRange = viewModel.calculatorNormalRange()
                binding.assessmentRange.text = "%.1f %s - %.1f %s".format(
                    normalRange.min,
                    normalRange.unit,
                    normalRange.max,
                    normalRange.unit
                )
                binding.assessmentDifference.text =
                    "(%s%.1f %s)".format(
                        normalRange.sign,
                        normalRange.difference,
                        normalRange.unit
                    )
            }

            val gradeList = BmiUtil.getGradeList(this, record.age, record.gender)
            val levelIndex =
                if (record.age > 20) BmiUtil.getGradeIndex(this, bmiInfo.levelName) - 1
                else BmiUtil.getGradeIndex(this, bmiInfo.levelName) - 3

            gradeList[levelIndex].isSelect = true
            gradeAdapter.update(gradeList)
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

        val timeText = TimeUtil(this).parseTimeStamp(bmiRecord?.customTime ?: 0)
        val text =
            "${timeText.selectMonth} ${timeText.selectDay} ${timeText.selectYear}  ${timeText.selectPeriod}"
        binding.resultMergeAd.tvTimeTag.text = text
        if (statusRecent) {
            // 历史结果图
            binding.resultGradeRv.visibility = View.GONE
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
                binding.resultGradeRv.visibility = View.GONE
                binding.resultMergeAd.tvTimeTag.visibility = View.GONE

            }
        }
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
                if (statusRecent) {
                    viewModel.deleteBmiRecord(bmiRecord!!)
                    sum--
                }
                if (statusRecent && sum.toInt() == 0) {
                    val intent = Intent(this@ResultActivity, DataInputActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    alertDialog.dismiss()
                    finish()
                }
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
        val recycler = rootView.findViewById<RecyclerView>(R.id.dialog_grade_rv)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        val gradeList = BmiUtil.getGradeList(this, record.age, record.gender)
        val gradeList = emptyList<Grade>()
        recycler.adapter = GradeAdapter(gradeList)

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
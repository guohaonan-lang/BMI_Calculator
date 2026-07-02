package com.example.bmicalculator.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.databinding.ActivityResultBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.viewmodel.BmiViewModel
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var bmiRecord: BmiEntity? = null
    private lateinit var alertDialog: AlertDialog
    private var status: String = ""
    private val viewModel: BmiViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        BmiViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

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
        bmiRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("BMI", BmiEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("BMI")
        }
        status = intent.getStringExtra("FATHER").toString()

        // 2. 非空校验并渲染数据（示例，自行对应布局TextView）
        bmiRecord?.let { record ->

            // BMI数值
            binding.resultMergeResult.mergeResultBmi.text = String.format("%.1f", record.bmiValue)
            binding.resultMergeResult.mergeBmiGauge.currentBmi = record.bmiValue
            // 身高体重
            binding.resultMergeResult.mergeResultHeight.text =
                String.format("%.1f cm", record.height)
            binding.resultMergeResult.mergeResultWeight.text =
                String.format("%.1f kg", record.weight)

            // 年龄
            binding.resultMergeResult.mergeResultAge.text = record.age.toString()
            // 性别
            binding.resultMergeResult.mergeResultGender.text =
                if (record.gender == 1) "Male" else "Female"


            val wheel = binding.resultMergeResult.mergeBmiGauge
            // 给仪表盘赋值
            wheel.age = record.age
            wheel.gender = record.gender
            wheel.currentBmi = record.bmiValue
            // 同步获取BMI评估文字，展示页面等级文本
            val bmiInfo = BmiUtil.getBmiFullInfo(record.age, record.gender, record.bmiValue)
            binding.resultMergeResult.mergeResultGrade.text = bmiInfo.levelName
            // 可同步设置文字颜色
            binding.resultMergeResult.mergeResultGrade.backgroundTintList =
                android.content.res.ColorStateList.valueOf(bmiInfo.colorInt)

        } ?: run {
            // 无数据返回输入页
            Toast.makeText(this, "数据传递失败！！！", Toast.LENGTH_SHORT).show()
            finish()
        }

        initDeleteDialog()

        //判断不同的页面，控制部分控件显隐
        if (status == "RecentActivity") {
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

        } else {

            binding.resultDelete.setOnClickListener {
                alertDialog.show()
            }

            onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

            setupBmiGardAndAssessment()

            binding.resultSave.setOnClickListener {
                lifecycleScope.launch {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.insertBmiRecord(bmiRecord!!)
                    }
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

            if (status == "InputActivity") {
                binding.resultMergeAd.root.visibility = View.GONE
            } else {
                binding.resultMergeGrade.root.visibility = View.GONE
                binding.resultMergeAd.tvTimeTag.visibility = View.GONE

            }
        }

    }

    private fun setupBmiGardAndAssessment() {
//        TODO("Not yet implemented")
    }

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
                viewModel.deleteBmiRecord(bmiRecord!!)
            }
            if (status == "RecentActivity" && sum.toInt() == 0) {
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
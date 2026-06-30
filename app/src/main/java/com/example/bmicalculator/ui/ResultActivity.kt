package com.example.bmicalculator.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivityResultBinding
import com.example.bmicalculator.model.BmiEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var bmiRecord: BmiEntity? = null
    private lateinit var alertDialog : AlertDialog

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

        // 2. 非空校验并渲染数据（示例，自行对应布局TextView）
        bmiRecord?.let { record ->

            // BMI数值
            binding.resultMergeResult.mergeResultBmi.text = String.format("%.1f", record.bmiValue)
            binding.resultMergeResult.mergeBmiGauge.currentBmi = record.bmiValue
            // 身高体重
            binding.resultMergeResult.mergeResultHeight.text = String.format("%.1f cm", record.height)
            binding.resultMergeResult.mergeResultWeight.text = String.format("%.1f kg", record.weight)

            // 年龄
            binding.resultMergeResult.mergeResultAge.text = record.age.toString()
            // 性别
            binding.resultMergeResult.mergeResultGender.text = if(record.gender == 1) "Male" else "Female"

            // 用 customTime 反向解析日期+时段
//            val displayTime = formatFullTime(record.customTime)


        } ?: run {
            // 无数据返回输入页
            Toast.makeText(this,"数据传递失败！！！",Toast.LENGTH_SHORT).show()
            finish()
        }

        initDeleteDialog()
        binding.resultDelete.setOnClickListener {
            alertDialog.show()
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setupBmiGardAndAssessment()
        binding.resultSave.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    private fun setupBmiGardAndAssessment() {
//        TODO("Not yet implemented")
    }

    // 解析出 Morning/Afternoon/Evening/Night
    private fun getPeriodFromTimeStamp(timeStamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..11 -> "Morning"
            hour in 12..17 -> "Afternoon"
            hour in 18..22 -> "Evening"
            else -> "Night"
        }
    }

    // 拼接成目标格式：May 6, 2021 Morning
    private fun formatFullTime(timeStamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
        val dateText = sdf.format(Date(timeStamp))
        val period = getPeriodFromTimeStamp(timeStamp)
        return "$dateText $period"
    }
    private fun initDeleteDialog(){
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_delete, null)

        alertDialog = AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setCancelable(true)
            .create()

        val buttonYes = dialogLayout.findViewById<TextView>(R.id.delete)
        val buttonNo = dialogLayout.findViewById<TextView>(R.id.cancel)
        buttonNo.setOnClickListener { alertDialog.dismiss() }
        buttonYes.setOnClickListener {
            alertDialog.dismiss()
            finish()
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 当用户手势返回时，这里会被触发，从而代替系统默认的退出行为
            alertDialog.show()
        }
    }
}
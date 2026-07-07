package com.example.bmicalculator.ui

import android.animation.ValueAnimator
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
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivityResultBinding
import com.example.bmicalculator.model.BmiEntity
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

        initData()

        initDeleteDialog()

        //判断不同的页面，控制部分控件显隐
        initChangePage()


//        binding.resultAssessment.text2 = "Normal Weight for your height (180cm):"
    }

    private fun initData() {
        // 1.读取数据
        bmiRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("BMI", BmiEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("BMI")
        }
        status = intent.getStringExtra("FATHER").toString()

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
            binding.resultMergeResult.mergeResultHeight.text =
                String.format("%.1f cm", record.height)
            binding.resultMergeResult.mergeResultWeight.text =
                String.format("%.1f kg", record.weight)

            binding.resultMergeResult.mergeResultAge.text = record.age.toString()
            binding.resultMergeResult.mergeResultGender.text =
                if (record.gender == 1) getString(R.string.male) else getString(R.string.female)


            val wheel = binding.resultMergeResult.mergeBmiGauge
            wheel.age = record.age
            wheel.gender = record.gender
            wheel.currentBmi = record.bmiValue

            val bmiInfo = BmiUtil.getBmiFullInfo(record.age, record.gender, record.bmiValue)
            binding.resultMergeResult.mergeResultGrade.text = bmiInfo.levelName
            binding.resultMergeResult.mergeResultGrade.backgroundTintList =
                android.content.res.ColorStateList.valueOf(record.bmiColor)

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
            val btn = binding.resultMergeResult.mergeResultGrade
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
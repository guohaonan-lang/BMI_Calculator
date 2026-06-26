package com.example.bmicalculator.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.format.Time
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.InputAgeAdapter
import com.example.bmicalculator.databinding.ActivityDataInputBinding
import com.example.bmicalculator.model.BmiEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Calendar

class DataInputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataInputBinding

    private var data : BmiEntity = BmiEntity(1,140.00f,65.00f,33f,25,0,436,5325325)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupWeightAndHeight()
        setupTime()
        setupAgeRecyclerView()
    }

    private fun setupTime() {
        var dayinput = binding.mergeDateInput.inputTime1
        val timeinput = binding.mergeDateInput.inputTime2

        dayinput.setOnClickListener {
            showDatePickerDialog(this) { month, day, year ->
                // 这里是选中日期后的回调
                val resultText = "$month $day, $year"
                binding.mergeDateInput.inputTime1.text = resultText
            }
        }
    }

    fun showDatePickerDialog(context: Context, onSelect: (month: String, day: Int, year: Int) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_date_picker, null)
        val dialog = Dialog(context, R.style.BottomDialogStyle)
        dialog.setContentView(dialogView)

        // 绑定控件
        val npMonth = dialogView.findViewById<NumberPicker>(R.id.np_month)
        val npDay = dialogView.findViewById<NumberPicker>(R.id.np_day)
        val npYear = dialogView.findViewById<NumberPicker>(R.id.np_year)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnDone = dialogView.findViewById<Button>(R.id.btn_done)

        // 1. 初始化月份数据
        val monthArr = arrayOf("Jan","Feb","Mar","Apr","May","June","July","Aug","Sep","Oct","Nov","Dec")
        npMonth.minValue = 0
        npMonth.maxValue = monthArr.size - 1
        npMonth.displayedValues = monthArr
        npMonth.wrapSelectorWheel = false

        // 2. 年份范围：2016~2022（截图示例区间，可自定义）
        npYear.minValue = 2016
        npYear.maxValue = 2030
        npYear.wrapSelectorWheel = false

        // 3. 联动逻辑：切换月份更新日期最大值
        fun refreshDayMax(monthPos: Int, year: Int) {
            val maxDay = when(monthPos) {
                1 -> if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) 29 else 28
                0,2,4,6,7,9,11 -> 31
                else -> 30
            }
            npDay.minValue = 1
            npDay.maxValue = maxDay
        }
        // 默认初始值
        npDay.minValue = 1
        npDay.maxValue = 31
        npDay.wrapSelectorWheel = false

        // 月份滚动监听
        npMonth.setOnValueChangedListener { _, newVal, _ ->
            refreshDayMax(newVal, npYear.value)
        }
        // 年份滚动监听
        npYear.setOnValueChangedListener { _, newVal, _ ->
            refreshDayMax(npMonth.value, newVal)
        }

        // 按钮事件
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnDone.setOnClickListener {
            val selMonth = monthArr[npMonth.value]
            val selDay = npDay.value
            val selYear = npYear.value
            onSelect(selMonth, selDay, selYear)
            dialog.dismiss()
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonthIdx = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val targetYear = currentYear.coerceIn(npYear.minValue, npYear.maxValue)
        npMonth.value = currentMonthIdx
        npYear.value = targetYear
        refreshDayMax(currentMonthIdx, targetYear)
        npDay.value = currentDay

        // 底部弹出样式
        val window = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.BOTTOM)
        dialog.show()
    }
    private fun setupWeightAndHeight() {
        val edtWeight = binding.mergeDateInput.inputWeight
        val edtHeight = binding.mergeDateInput.inputHeight

        val heightFloat: Float = edtHeight.text.toString().toFloat()
        val weightFloat: Float = edtHeight.text.toString().toFloat()

        data.height = heightFloat
        data.weight = weightFloat
    }

    private fun setupAgeRecyclerView() {
        val ageRecyclerView = binding.mergeDateInput.inputAge
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        ageRecyclerView.layoutManager = layoutManager
        val adapter = InputAgeAdapter(ageRecyclerView){

        }
        ageRecyclerView.adapter = adapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(ageRecyclerView)

        // ========== 滚动透明度监听，全部放入函数内 ==========
        ageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private val minAlpha = 0f
            private val maxAlpha = 1f
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lm = recyclerView.layoutManager as LinearLayoutManager
                val rvCenterX = recyclerView.width / 2f
                val firstVisible = lm.findFirstVisibleItemPosition()
                val lastVisible = lm.findLastVisibleItemPosition()

                for (pos in firstVisible-1..lastVisible+1) {
                    val itemView = lm.findViewByPosition(pos) ?: continue
                    val itemCenterX = itemView.left + itemView.width / 2f
                    val distance = kotlin.math.abs(itemCenterX - rvCenterX)
                    val maxDistance = recyclerView.width / 2f
                    var ratio = 1 - (distance / maxDistance)
                    ratio = ratio.coerceAtLeast(0f)

                    // 透明度计算
                    val alpha = minAlpha + maxAlpha * ratio

                    itemView.alpha = alpha
                }
            }
        })

        // 页面加载完成后手动刷新一次透明度（刚进来没滑动时生效）
        ageRecyclerView.post {
            ageRecyclerView.scrollBy(1, 0)
            ageRecyclerView.scrollBy(-1, 0)
        }
    }
}
package com.example.bmicalculator.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentBmiBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.ui.RecentActivity
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.viewmodel.BmiViewModel
import kotlinx.coroutines.launch

class BmiFragment : Fragment() {

    private var _binding: FragmentBmiBinding? = null
    private val binding get() = checkNotNull(_binding)

    private var bmiRecord: BmiEntity? = null

    private val viewModel: BmiViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        BmiViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBmiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultContent.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.binding.mainViewpage2.currentItem = 0
        }

        binding.recent.setOnClickListener {
            val intent = Intent(requireContext(), RecentActivity::class.java)
            startActivity(intent)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            bmiRecord = viewModel.getLatestBmi()
            updateBmi()
        }


    }

    private fun updateBmi() {
        val wheel = binding.resultMergeResult.mergeBmiGauge
        // 给仪表盘赋值
        bmiRecord?.let { record ->
            wheel.age = record.age
            wheel.gender = record.gender
            wheel.currentBmi = record.bmiValue

            binding.resultTime.text = record.timeText
            binding.resultMergeResult.mergeResultBmi.text = String.format("%.1f", record.bmiValue)

            val bmiInfo =
                BmiUtil.getBmiFullInfo(requireContext(), record.age, record.gender, record.bmiValue)
            binding.resultMergeResult.mergeResultGrade.text = bmiInfo.levelName
            binding.resultMergeResult.mergeResultGrade.backgroundTintList =
                ColorStateList.valueOf(record.bmiColor)
            initGrade()
            setupBmiGard(bmiInfo.levelName)
        }
    }

    private fun setupBmiGard(levelName: String) {
        var bmiRanges: FloatArray
        var grad = 0
        when (levelName) {
            "Very Severely Underweight" -> grad = 1
            "Severely Underweight" -> grad = 2
            "Underweight" -> grad = 3
            "Normal" -> grad = 4
            "Overweight" -> grad = 5
            "Obese Class I" -> grad = 6
            "Obese Class II" -> grad = 7
            "Obese Class III" -> grad = 8
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

    private fun switchTeenGrade(bmiRanges: FloatArray) {
        val rootLayout = binding.resultMergeGrade.root
        val ctx = rootLayout.context

        // 重置全部条目
        for (i in 1..8) {
            val resetId = ctx.resources.getIdentifier("merge_grade_list_$i", "id", ctx.packageName)
            val resetItem = rootLayout.findViewById<ViewGroup>(resetId)
            val scopeId = ctx.resources.getIdentifier("merge_grade_list_scope_$i", "id", ctx.packageName)
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
            rootLayout.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(itemId)

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
        val font = ResourcesCompat.getFont(requireContext(), R.font.font_extrabold)
        textTv.typeface = font
        scopeTv.typeface = font
        textTv.alpha = 1f
        scopeTv.alpha = 1f
    }

    private fun initGrade() {
        val rootLayout = binding.resultMergeGrade.root
        val ctx = rootLayout.context
        val blackState = ColorStateList.valueOf(ctx.getColor(R.color.black))
        val font = ResourcesCompat.getFont(requireContext(), R.font.font_regular)
        // 重置全部条目
        for (i in 1..8) {
            val itemId = ctx.resources.getIdentifier("merge_grade_list_$i", "id", ctx.packageName)
            val itemRoot = rootLayout.findViewById<ViewGroup>(itemId)

            // 1. 清除根布局高亮底色（只作用外层ConstraintLayout，不影响圆点）
            ViewCompat.setBackgroundTintList(itemRoot, null)

            // 找到内部控件
            val colorViewId = ctx.resources.getIdentifier("merge_grade_list_color_$i", "id", ctx.packageName)
            val textId = ctx.resources.getIdentifier("merge_grade_list_text_$i", "id", ctx.packageName)
            val scopeId = ctx.resources.getIdentifier("merge_grade_list_scope_$i", "id", ctx.packageName)

            val colorView = itemRoot.findViewById<View>(colorViewId)
            val textTv = itemRoot.findViewById<TextView>(textId)
            val scopeTv = itemRoot.findViewById<TextView>(scopeId)

            val s = when (i) {
                1 -> getString(R.string.adults_bmi_range_VerySeverelyUnderweight)
                2 -> getString(R.string.adults_bmi_range_SeverelyUnderweight)
                3 -> getString(R.string.adults_bmi_range_Underweight)
                4 -> getString(R.string.adults_bmi_range_normal)
                5 -> getString(R.string.adults_bmi_range_overweight)
                6 -> getString(R.string.adult_bmi_range_obese_class_i)
                7 -> getString(R.string.adult_bmi_range_obese_class_ii)
                8 -> getString(R.string.adult_bmi_range_obese_class_iii)
                else -> "" // 必须兜底，防止无匹配返回空
            }
            scopeTv.text = s

            // 2. 文字控件：清空白色背景、恢复黑色文字
            textTv.setBackgroundColor(0)
            scopeTv.setBackgroundColor(0)
            textTv.setTextColor(blackState)
            scopeTv.setTextColor(blackState)
            textTv.typeface = font
            scopeTv.typeface = font
            textTv.alpha = 0.7f
            scopeTv.alpha = 0.7f

            // ========== 关键修复：恢复圆点原有gradN颜色 ==========
            // 不要调用 colorView.setBackgroundColor(0)，这会毁掉圆形drawable
            val gradColorRes = ctx.resources.getIdentifier("grad$i", "color", ctx.packageName)
            val gradTint = ColorStateList.valueOf(ctx.getColor(gradColorRes))
            ViewCompat.setBackgroundTintList(colorView, gradTint)
            itemRoot.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            val newBmiRecord = viewModel.getLatestBmi()
            if (newBmiRecord != bmiRecord) {
                bmiRecord = newBmiRecord
                updateBmi()
            }

        }
    }

}
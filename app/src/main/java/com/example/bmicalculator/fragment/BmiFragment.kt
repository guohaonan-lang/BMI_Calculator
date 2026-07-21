package com.example.bmicalculator.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bmicalculator.R
import com.example.bmicalculator.adapter.GradeAdapter
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentBmiBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.ui.RecentActivity
import com.example.bmicalculator.util.BmiUtil
import com.example.bmicalculator.util.TimeUtil
import com.example.bmicalculator.viewmodel.BmiFragmentViewModel
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n", "DefaultLocale")
class BmiFragment : Fragment() {

    private var _binding: FragmentBmiBinding? = null
    private val binding get() = checkNotNull(_binding)
    private lateinit var gradeAdapter: GradeAdapter

    private val viewModel: BmiFragmentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        BmiFragmentViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBmiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 点击转换到输入页
        binding.resultContent.setOnClickListener {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.binding.mainViewpage2.currentItem = 0
        }
        // 点击跳转历史记录
        binding.recent.setOnClickListener {
            val intent = Intent(requireContext(), RecentActivity::class.java)
            startActivity(intent)
        }
        initGradeRecyclerView()
        initDataFlow()
    }
    private fun initDataFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.latestBmiRecord.collect { record ->
                    record?.apply {
                        updateBmi(record)
                    }
                }
            }
        }
    }

    private fun initGradeRecyclerView() {
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.resultGradeRv.layoutManager = layoutManager
        gradeAdapter = GradeAdapter(emptyList())
        binding.resultGradeRv.adapter = gradeAdapter
    }



    // 读取信息
    private fun updateBmi(record: BmiEntity) {
        val bmiInfo =
            BmiUtil.getBmiFullInfo(requireContext(), record.age, record.gender, record.bmiValue)
        binding.resultMergeResult.apply {
            mergeBmiGauge.age = record.age
            mergeBmiGauge.gender = record.gender
            mergeBmiGauge.currentBmi = record.bmiValue
            val unit = if (record.weightUnit) "kg" else "lb"
            mergeResultWeight.text = "${record.weight} $unit"

            mergeResultHeight.text = if (record.heightUnit) "${record.height} cm"
            else "${record.heightFt}ft ${record.heightIn}in"
            mergeResultBmi.text = String.format("%.1f", record.bmiValue)
            mergeResultGender.text =
                if (record.gender == 1) getString(R.string.male) else getString(R.string.female)
            mergeResultAge.text = record.age.toString() + getString(R.string.years_old)
            mergeResultGrade.text = bmiInfo.levelName
            mergeResultGrade.backgroundTintList =
                ColorStateList.valueOf(record.bmiColor)
        }

        val timeText = TimeUtil(requireContext()).parseTimeStamp(record.customTime)
        binding.resultTime.text =
            "${timeText.selectMonth} ${timeText.selectDay} ${timeText.selectYear}"

        val gradeList = BmiUtil.getGradeList(requireContext(), record.age, record.gender)
        val levelIndex =
            if (record.age > 20) BmiUtil.getGradeIndex(requireContext(), bmiInfo.levelName) - 1
            else BmiUtil.getGradeIndex(requireContext(), bmiInfo.levelName) - 3
        gradeList[levelIndex].isSelect = true
        gradeAdapter.update(gradeList)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
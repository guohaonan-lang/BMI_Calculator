package com.example.bmicalculator.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentBmiBinding
import com.example.bmicalculator.model.BmiEntity
import com.example.bmicalculator.ui.MainActivity
import com.example.bmicalculator.ui.RecentActivity
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
            binding.resultMergeResult.mergeResultGrade.text = record.bmiGrade
            binding.resultMergeResult.mergeResultGrade.backgroundTintList =
                android.content.res.ColorStateList.valueOf(record.bmiColor)
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
package com.example.bmicalculator.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentDataInputBinding
import com.example.bmicalculator.viewmodel.InputViewModel

@SuppressLint("DefaultLocale", "SetTextI18n")
class DataInputFragment : Fragment() {

    private var _binding: FragmentDataInputBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: InputViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        InputViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ========== 初始化 Compose 容器 ==========
        binding.inputComposeView.apply {
            // 设置 Compose 的 Dispose 策略，随 Fragment View 的生命周期自动销毁
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    InputScreen(viewModel)
                }
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
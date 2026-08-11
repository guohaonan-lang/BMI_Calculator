package com.example.bmicalculator.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.FragmentDataInputBinding
import com.example.bmicalculator.ui.ResultActivity
import com.example.bmicalculator.viewmodel.InputFragmentViewModel
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale", "SetTextI18n")
class DataInputFragment : Fragment() {

    private var _binding: FragmentDataInputBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: InputFragmentViewModel by viewModels {
        val db = BmiDatabase.getDatabase(requireContext())
        InputFragmentViewModel.provideFactory(BmiRepository(db.bmiDao()))
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
                    InputScreen(viewModel = viewModel)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.effect.collect {effect ->
                    when (effect) {
                        is InputFragmentViewModel.InputEffect.ShowToast ->
                            effect.msgResId?.let { id ->
                                val str = this@DataInputFragment.getString(id)
                                Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                            }

                        is InputFragmentViewModel.InputEffect.NavToResult -> {
                            val intent = Intent(context, ResultActivity::class.java)
                            intent.putExtra("BMI", effect.bmiEntity)
                            intent.putExtra("FATHER", effect.isFirst)
                            this@DataInputFragment.startActivity(intent)
                        }
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
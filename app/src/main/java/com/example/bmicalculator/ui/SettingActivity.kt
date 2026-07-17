package com.example.bmicalculator.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bmicalculator.R
import com.example.bmicalculator.data.BmiDatabase
import com.example.bmicalculator.data.BmiRepository
import com.example.bmicalculator.databinding.ActivitySettingBinding
import com.example.bmicalculator.viewmodel.SettingViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun inflateBinding(inflater: LayoutInflater): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(inflater)
    }

    private val viewModel: SettingViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        SettingViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initAllClick()
        initDataFlow()
    }

    private fun initDataFlow() {

    }

    private fun initAllClick() {
        binding.settingUser.setOnClickListener {
            initUserDialog()
        }
        binding.settingLanguage.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
        }
        binding.settingUserAutorenew.setOnClickListener {
            initAutoDialog()
            lifecycleScope.launch {
                viewModel.readTestFile(this@SettingActivity)
            }
        }

        binding.settingFeedback.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }
        binding.settingBack.setOnClickListener {
            finish()
        }
    }

    private fun initUserDialog() {
        val userBottomSheetDialog = BottomSheetDialog(this)
        val rootView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_setting_user, null)
        userBottomSheetDialog.setContentView(rootView)
        rootView.findViewById<Button>(R.id.user_cancel_bt).setOnClickListener {
            userBottomSheetDialog.dismiss()
        }
        rootView.findViewById<ImageView>(R.id.user_cancel_x).setOnClickListener {
            userBottomSheetDialog.dismiss()
        }
        rootView.findViewById<Button>(R.id.user_login).setOnClickListener {
            if (rootView.findViewById<Button>(R.id.user_login).text == getString(R.string.log_out)) {
                rootView.findViewById<Button>(R.id.user_login).text = getString(R.string.log_in)
                rootView.findViewById<Button>(R.id.user_login).setTextColor(getColor(R.color.red))
                userBottomSheetDialog.dismiss()
                binding.userIv.visibility = View.GONE
                binding.settingUserText1.text = getString(R.string.setting_backup_restore)
                binding.settingUserText2.text = getString(R.string.setting_synchronize_your_data)

            } else {
                rootView.findViewById<Button>(R.id.user_login).text = getString(R.string.log_out)
                rootView.findViewById<Button>(R.id.user_login).setTextColor(getColor(R.color.red))
                userBottomSheetDialog.dismiss()
                binding.userIv.visibility = View.VISIBLE
                binding.settingUserText1.text = "Cassie"
                binding.settingUserText2.text = "cassiexiao@gmail.com"
                binding.settingUserText2.alpha = 0.5f
            }

        }
        userBottomSheetDialog.show()
    }

    private fun initAutoDialog() {
        val autoDialog = Dialog(this)
        val rootView = LayoutInflater.from(this).inflate(R.layout.dialog_autorenew, null)
        autoDialog.setContentView(rootView)
        rootView.findViewById<Button>(R.id.auto_done).setOnClickListener {

            autoDialog.dismiss()
        }
        autoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        autoDialog.show()
    }

}
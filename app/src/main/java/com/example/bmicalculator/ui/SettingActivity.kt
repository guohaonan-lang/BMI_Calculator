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
import androidx.appcompat.app.AppCompatActivity
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

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var userBottomSheetDialog: BottomSheetDialog
    private lateinit var autoDialog: Dialog
    private val viewModel: SettingViewModel by viewModels {
        val db = BmiDatabase.getDatabase(this)
        SettingViewModel.provideFactory(BmiRepository(db.bmiDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initUserDialog()
        initAutoDialog()
        initAllClick()
    }

    private fun initAllClick() {
        binding.settingUser.setOnClickListener {
            userBottomSheetDialog.show()
        }
        binding.settingLanguage.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            startActivity(intent)
        }
        binding.settingUserAutorenew.setOnClickListener {
            autoDialog.show()
            lifecycleScope.launch {
                val bmiList = viewModel.getAllList()
                viewModel.exportFile(this@SettingActivity, bmiList)
            }
        }

        binding.settingFeedback.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initUserDialog() {
        userBottomSheetDialog = BottomSheetDialog(this)
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
    }

    private fun initAutoDialog() {
        autoDialog = Dialog(this)
        val rootView = LayoutInflater.from(this).inflate(R.layout.dialog_autorenew, null)
        autoDialog.setContentView(rootView)
        rootView.findViewById<Button>(R.id.auto_done).setOnClickListener {

            autoDialog.dismiss()
        }
        autoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}
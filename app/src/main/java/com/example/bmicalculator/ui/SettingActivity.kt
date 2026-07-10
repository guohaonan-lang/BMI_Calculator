package com.example.bmicalculator.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bmicalculator.R
import com.example.bmicalculator.databinding.ActivitySettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private lateinit var userBottomSheetDialog: BottomSheetDialog
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
    }

    private fun initUserDialog() {
        userBottomSheetDialog = BottomSheetDialog(this)
        val rootView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_setting_user,null)
        userBottomSheetDialog.setContentView(rootView)
        rootView.findViewById<Button>(R.id.user_cancel_bt).setOnClickListener {
            userBottomSheetDialog.dismiss()
        }
        rootView.findViewById<ImageView>(R.id.user_cancel_x).setOnClickListener {
            userBottomSheetDialog.dismiss()
        }
        rootView.findViewById<Button>(R.id.user_login).setOnClickListener {
            rootView.findViewById<Button>(R.id.user_login).text = "Log out"
            rootView.findViewById<Button>(R.id.user_login).setTextColor(getColor(R.color.red))
            userBottomSheetDialog.dismiss()
        }
    }

}
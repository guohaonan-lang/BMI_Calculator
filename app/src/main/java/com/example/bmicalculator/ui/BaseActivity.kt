package com.example.bmicalculator.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.bmicalculator.util.LangHelper

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    lateinit var binding: VB
    private var currentLang: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateBinding(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
    }

    abstract fun inflateBinding(inflater: LayoutInflater): VB

    override fun attachBaseContext(newBase: Context) {
        val selectLang = LangHelper.getSavedLang(newBase)
        currentLang = selectLang
        val context = LangHelper.attachBaseContext(newBase, selectLang)
        super.attachBaseContext(context)
    }

    protected fun switchLanguage(context: Context, langCode: String) {
        if (langCode != currentLang) {
            LangHelper.setLanguage(context, langCode)
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        val selectLang = LangHelper.getSavedLang(this)
        if (selectLang != currentLang) recreate()
    }
}
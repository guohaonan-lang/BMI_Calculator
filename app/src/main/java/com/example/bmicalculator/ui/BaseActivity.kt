package com.example.bmicalculator.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.bmicalculator.util.LangHelper

abstract class BaseActivity : ComponentActivity() {
    private var currentLang: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }


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
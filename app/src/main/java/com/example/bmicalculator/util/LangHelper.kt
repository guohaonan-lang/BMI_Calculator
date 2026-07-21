package com.example.bmicalculator.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LangHelper {
    private const val SP_NAME = "lang_sp"
    private const val KEY_LANG = "select_lang"

    // 语言标识
    const val LANG_EN = "en"
    const val LANG_ZH = "zh"

    // 保存语言
    fun setLanguage(context: Context, langCode: String) {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_LANG, langCode).apply()
    }

    // 获取上次保存的语言
    fun getSavedLang(context: Context): String {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_LANG, LANG_EN) ?: LANG_EN
    }

    // 必须传入 baseContext
    fun attachBaseContext(baseContext: Context, langCode: String): Context {
        val config = baseContext.resources.configuration

        val locale = when (langCode) {
            LANG_ZH -> Locale.CHINA
            else -> Locale.ENGLISH
        }

        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            config.setLocales(LocaleList(locale))
        }

        return baseContext.createConfigurationContext(config)
    }
}
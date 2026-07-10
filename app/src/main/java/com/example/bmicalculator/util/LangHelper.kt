package com.example.bmicalculator.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LangHelper {
    private const val SP_NAME = "lang_sp"
    private const val KEY_LANG = "select_lang"

    // 语言标识
    const val LANG_EN = "en"
    const val LANG_ZH = "zh"

    // 保存并切换语言
    fun setLanguage(context: Context, langCode: String) {
        // 保存到本地SP
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_LANG, langCode).apply()

        // 创建对应地区
        val locale = when (langCode) {
            LANG_ZH -> Locale.CHINA
            else -> Locale.ENGLISH
        }

        // 更新全局配置
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val res = context.resources
        res.updateConfiguration(config, res.displayMetrics)
    }

    // 获取上次保存的语言
    fun getSavedLang(context: Context): String {
        val sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_LANG, LANG_EN) ?: LANG_EN
    }
}
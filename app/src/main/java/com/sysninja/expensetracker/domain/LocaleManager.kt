package com.sysninja.expensetracker.domain

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sysninja.expensetracker.ExpenseTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object LocaleManager {

    const val LANG_EN = "en"
    const val LANG_BN = "bn"

    /**
     * Initializes the locale from preferences or defaults to English.
     */
    fun initAppLocale(app: ExpenseTrackerApp) {
        CoroutineScope(Dispatchers.Main).launch {
            val savedLang = app.preferencesManager.languageFlow.first()
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val currentTag = if (!currentLocales.isEmpty) currentLocales.toLanguageTags() else ""

            val targetLang = if (savedLang.startsWith("bn", ignoreCase = true)) LANG_BN else LANG_EN
            if (!currentTag.startsWith(targetLang, ignoreCase = true)) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetLang))
            }
        }
    }

    /**
     * Switches the app language, saves to preferences, and applies the locale.
     */
    fun switchLanguage(context: Context, newLang: String, onComplete: (() -> Unit)? = null) {
        val app = context.applicationContext as ExpenseTrackerApp
        val targetLang = if (newLang.startsWith("bn", ignoreCase = true)) LANG_BN else LANG_EN

        CoroutineScope(Dispatchers.Main).launch {
            app.preferencesManager.setLanguage(targetLang)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetLang))
            onComplete?.invoke()
        }
    }

    /**
     * Toggles between English and Bangla.
     */
    fun toggleLanguage(context: Context, onComplete: ((String) -> Unit)? = null) {
        val current = getCurrentLanguage()
        val next = if (current == LANG_BN) LANG_EN else LANG_BN
        switchLanguage(context, next) {
            onComplete?.invoke(next)
        }
    }

    /**
     * Gets current language code: "en" or "bn"
     */
    fun getCurrentLanguage(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (!appLocales.isEmpty) {
            val tag = appLocales.toLanguageTags()
            return if (tag.startsWith("bn", ignoreCase = true)) LANG_BN else LANG_EN
        }
        return LANG_EN
    }

    /**
     * Human readable display label for toggle button.
     */
    fun getToggleLabel(): String {
        return if (getCurrentLanguage() == LANG_BN) "EN | বাং" else "বাং | EN"
    }
}

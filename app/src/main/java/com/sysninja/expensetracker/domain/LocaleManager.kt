package com.sysninja.expensetracker.domain

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.sysninja.expensetracker.ExpenseTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

object LocaleManager {

    const val LANG_EN = "en"
    const val LANG_BN = "bn"

    private const val PREF_NAME = "locale_preferences"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Gets the currently persisted language synchronously. Default is English ("en").
     */
    fun getPersistedLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_SELECTED_LANGUAGE, LANG_EN) ?: LANG_EN
    }

    /**
     * Initializes the locale from preferences or defaults to English.
     */
    fun initAppLocale(app: ExpenseTrackerApp) {
        val lang = getPersistedLanguage(app)
        applyLocaleDirect(app, lang)
        val appLocales = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    /**
     * Applies locale configuration directly to resources.
     */
    fun applyLocaleDirect(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
        return context.createConfigurationContext(config)
    }

    /**
     * Switches the app language, saves to preferences synchronously, and applies the locale.
     */
    fun switchLanguage(context: Context, newLang: String, onComplete: (() -> Unit)? = null) {
        val targetLang = if (newLang.startsWith("bn", ignoreCase = true)) LANG_BN else LANG_EN
        getPrefs(context).edit().putString(KEY_SELECTED_LANGUAGE, targetLang).commit()

        val app = context.applicationContext as? ExpenseTrackerApp
        app?.let {
            CoroutineScope(Dispatchers.Main).launch {
                it.preferencesManager.setLanguage(targetLang)
            }
        }

        applyLocaleDirect(context, targetLang)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetLang))

        onComplete?.invoke()

        if (context is Activity) {
            context.recreate()
        }
    }

    /**
     * Toggles between English and Bangla.
     */
    fun toggleLanguage(context: Context, onComplete: ((String) -> Unit)? = null) {
        val current = getCurrentLanguage(context)
        val next = if (current == LANG_BN) LANG_EN else LANG_BN
        switchLanguage(context, next) {
            onComplete?.invoke(next)
        }
    }

    /**
     * Gets current language code: "en" or "bn"
     */
    fun getCurrentLanguage(context: Context? = null): String {
        context?.let {
            return getPersistedLanguage(it)
        }
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
    fun getToggleLabel(context: Context? = null): String {
        return if (getCurrentLanguage(context) == LANG_BN) "English" else "বাংলা"
    }
}

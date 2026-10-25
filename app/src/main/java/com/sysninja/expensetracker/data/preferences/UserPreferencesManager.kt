package com.sysninja.expensetracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {

    companion object {
        val KEY_CURRENCY = stringPreferencesKey("currency_symbol")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_DEFAULT_PERIOD = stringPreferencesKey("default_period")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        val KEY_PIN_SALT = stringPreferencesKey("pin_salt")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_BUDGET_WARNING_THRESHOLD = intPreferencesKey("budget_warning_threshold")
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val currencySymbolFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY] ?: "৳"
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "en"
    }

    val defaultPeriodFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_PERIOD] ?: "THIS_MONTH"
    }

    val isPinEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_ENABLED] ?: false
    }

    val pinHashFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_HASH] ?: ""
    }

    val pinSaltFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PIN_SALT] ?: ""
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val isNotificationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val budgetWarningThresholdFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_BUDGET_WARNING_THRESHOLD] ?: 80
    }

    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { it[KEY_CURRENCY] = currency }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language }
    }

    suspend fun setDefaultPeriod(period: String) {
        context.dataStore.edit { it[KEY_DEFAULT_PERIOD] = period }
    }

    suspend fun setPin(hash: String, salt: String) {
        context.dataStore.edit {
            it[KEY_PIN_HASH] = hash
            it[KEY_PIN_SALT] = salt
            it[KEY_PIN_ENABLED] = true
        }
    }

    suspend fun disablePin() {
        context.dataStore.edit {
            it[KEY_PIN_HASH] = ""
            it[KEY_PIN_SALT] = ""
            it[KEY_PIN_ENABLED] = false
            it[KEY_BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setBudgetWarningThreshold(threshold: Int) {
        context.dataStore.edit { it[KEY_BUDGET_WARNING_THRESHOLD] = threshold }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name.trim() }
    }
}

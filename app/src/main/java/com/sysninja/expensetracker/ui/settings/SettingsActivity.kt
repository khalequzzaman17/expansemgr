package com.sysninja.expensetracker.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivitySettingsBinding
import com.sysninja.expensetracker.domain.LocaleManager
import com.sysninja.expensetracker.ui.about.AboutActivity
import com.sysninja.expensetracker.ui.backup.BackupRestoreActivity
import com.sysninja.expensetracker.ui.categories.CategoriesActivity
import com.sysninja.expensetracker.ui.debts.DebtsActivity
import com.sysninja.expensetracker.ui.export.ExportActivity
import com.sysninja.expensetracker.ui.recurring.RecurringActivity
import com.sysninja.expensetracker.ui.savings.SavingsActivity
import com.sysninja.expensetracker.ui.security.SetPinActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupClickListeners()
        updateLanguageUI()
        observePinStatus()
        observeUserProfile()
    }

    private fun observeUserProfile() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            app.preferencesManager.userNameFlow.collect { name ->
                val displayName = name.ifBlank { getString(R.string.default_user_name) }
                binding.tvProfileName.text = displayName
                binding.tvProfileAvatar.text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
            }
        }
    }

    private fun setupClickListeners() {
        binding.cvProfile.setOnClickListener {
            showEditNameDialog()
        }

        binding.cardModuleCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        binding.cardModuleSavings.setOnClickListener {
            startActivity(Intent(this, SavingsActivity::class.java))
        }

        binding.cardModuleDebts.setOnClickListener {
            startActivity(Intent(this, DebtsActivity::class.java))
        }

        binding.cardModuleRecurring.setOnClickListener {
            startActivity(Intent(this, RecurringActivity::class.java))
        }

        binding.cardExportData.setOnClickListener {
            startActivity(Intent(this, ExportActivity::class.java))
        }

        binding.cardBackupRestore.setOnClickListener {
            startActivity(Intent(this, BackupRestoreActivity::class.java))
        }

        binding.cardLanguagePreference.setOnClickListener {
            showLanguageDialog()
        }

        binding.cardPinSecurity.setOnClickListener {
            startActivity(Intent(this, SetPinActivity::class.java))
        }

        binding.cardAboutApp.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun showLanguageDialog() {
        val currentLang = LocaleManager.getCurrentLanguage()
        val languages = arrayOf(getString(R.string.english), getString(R.string.bangla))
        val checkedItem = if (currentLang == LocaleManager.LANG_BN) 1 else 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language_title))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLang = if (which == 1) LocaleManager.LANG_BN else LocaleManager.LANG_EN
                LocaleManager.switchLanguage(this, selectedLang) {
                    updateLanguageUI()
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateLanguageUI() {
        val current = LocaleManager.getCurrentLanguage(this)
        if (current == LocaleManager.LANG_BN) {
            binding.tvCurrentLanguage.text = getString(R.string.bangla)
            binding.tvLangBadge.text = "বাং"
        } else {
            binding.tvCurrentLanguage.text = getString(R.string.english)
            binding.tvLangBadge.text = "EN"
        }
    }

    private fun observePinStatus() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val isPinEnabled = app.preferencesManager.isPinEnabledFlow.first()
            binding.tvPinStatus.text = if (isPinEnabled) getString(R.string.pin_status_enabled) else getString(R.string.pin_status_disabled)
        }
    }

    private fun showEditNameDialog() {
        val app = application as ExpenseTrackerApp
        val container = android.widget.FrameLayout(this).apply {
            val padH = (24 * resources.displayMetrics.density).toInt()
            val padV = (12 * resources.displayMetrics.density).toInt()
            setPadding(padH, padV, padH, padV / 2)
        }
        val input = android.widget.EditText(this).apply {
            hint = getString(R.string.enter_name_hint)
            setText(binding.tvProfileName.text.toString().takeIf { it != getString(R.string.default_user_name) } ?: "")
            setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            val pad = (14 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            isSingleLine = true
        }
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_name_title))
            .setView(container)
            .setPositiveButton(getString(R.string.save_name_action)) { dialog, _ ->
                val entered = input.text.toString().trim()
                val finalName = if (entered.isNotBlank()) entered else getString(R.string.default_user_name)
                lifecycleScope.launch {
                    app.preferencesManager.setUserName(finalName)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateLanguageUI()
        observePinStatus()
    }
}

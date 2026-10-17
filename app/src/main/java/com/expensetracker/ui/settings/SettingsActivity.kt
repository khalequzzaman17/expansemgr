package com.expensetracker.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.expensetracker.R
import com.expensetracker.ui.about.AboutActivity
import com.expensetracker.ui.accounts.AccountsActivity
import com.expensetracker.ui.backup.BackupRestoreActivity
import com.expensetracker.ui.budgets.BudgetsActivity
import com.expensetracker.ui.categories.CategoriesActivity
import com.expensetracker.ui.debts.DebtsActivity
import com.expensetracker.ui.export.ExportActivity
import com.expensetracker.ui.recurring.RecurringActivity
import com.expensetracker.ui.savings.SavingsActivity
import com.expensetracker.ui.security.SetPinActivity
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@SettingsActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.settings_title)
            setTitleTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@SettingsActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@SettingsActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        fun addItem(title: String, iconRes: Int, onClick: () -> Unit) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(48, 40, 48, 40)
                gravity = android.view.Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true
                setBackgroundResource(android.R.drawable.list_selector_background)
                setOnClickListener { onClick() }
            }

            val tv = TextView(this).apply {
                text = title
                setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.text_primary))
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            itemLayout.addView(tv)
            root.addView(itemLayout)
        }

        addItem(getString(R.string.accounts_title), R.drawable.ic_wallet) {
            startActivity(Intent(this, AccountsActivity::class.java))
        }
        addItem(getString(R.string.categories_title), R.drawable.ic_category) {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
        addItem(getString(R.string.budgets_title), R.drawable.ic_budget) {
            startActivity(Intent(this, BudgetsActivity::class.java))
        }
        addItem(getString(R.string.recurring_title), R.drawable.ic_recurring) {
            startActivity(Intent(this, RecurringActivity::class.java))
        }
        addItem(getString(R.string.savings_title), R.drawable.ic_savings) {
            startActivity(Intent(this, SavingsActivity::class.java))
        }
        addItem(getString(R.string.debts_title), R.drawable.ic_debt) {
            startActivity(Intent(this, DebtsActivity::class.java))
        }
        addItem(getString(R.string.export_title), R.drawable.ic_export) {
            startActivity(Intent(this, ExportActivity::class.java))
        }
        addItem(getString(R.string.backup_restore_title), R.drawable.ic_backup) {
            startActivity(Intent(this, BackupRestoreActivity::class.java))
        }
        addItem(getString(R.string.set_pin), R.drawable.ic_lock) {
            startActivity(Intent(this, SetPinActivity::class.java))
        }
        addItem(getString(R.string.about_title), R.drawable.ic_info) {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        setContentView(root)
    }
}

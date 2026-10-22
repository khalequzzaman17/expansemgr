package com.sysninja.expensetracker.ui.settings

import android.content.Intent
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.ui.about.AboutActivity
import com.sysninja.expensetracker.ui.accounts.AccountsActivity
import com.sysninja.expensetracker.ui.backup.BackupRestoreActivity
import com.sysninja.expensetracker.ui.budgets.BudgetsActivity
import com.sysninja.expensetracker.ui.debts.DebtsActivity
import com.sysninja.expensetracker.ui.export.ExportActivity
import com.sysninja.expensetracker.ui.recurring.RecurringActivity
import com.sysninja.expensetracker.ui.savings.SavingsActivity
import com.sysninja.expensetracker.ui.security.SetPinActivity
import com.sysninja.expensetracker.ui.dp
import com.sysninja.expensetracker.ui.stitchToolbar

class SettingsActivity : AppCompatActivity() {
    private val primary by lazy { ContextCompat.getColor(this, R.color.teal_primary) }
    private val muted by lazy { ContextCompat.getColor(this, R.color.text_muted) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.bg_navy_dark))
        }
        root.addView(stitchToolbar("More Settings", "হিসাব খাতা • Hisab Khata", ::finish))
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(28))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        content.addView(profileCard())
        addSection(content, "ফিনান্স মডিউল • MODULES")
        val modules = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        addItem(modules, "ক্যাটাগরি সমূহ", "Income & Expense Tags", R.drawable.ic_category) { com.sysninja.expensetracker.ui.categories.CategoriesActivity::class.java }
        addItem(modules, "সঞ্চয় ও লক্ষ্য", "Savings Goals", R.drawable.ic_savings) { SavingsActivity::class.java }
        addItem(modules, "ধার-দেনা ও ঋণ", "Debts & Lending", R.drawable.ic_debt) { DebtsActivity::class.java }
        addItem(modules, "পুনরাবৃত্ত বিল", "Recurring Auto-log", R.drawable.ic_recurring) { RecurringActivity::class.java }
        content.addView(modules)
        addSection(content, "ডাটা ও ব্যাকআপ • DATA & BACKUP")
        addWideItem(content, "ডাটা এক্সপোর্ট (Export)", "Download ledger records", R.drawable.ic_export) { ExportActivity::class.java }
        addWideItem(content, "ব্যাকআপ ও রিস্টোর", "Keep your records safe", R.drawable.ic_backup) { BackupRestoreActivity::class.java }
        addSection(content, "পছন্দ ও ভাষা • PREFERENCES")
        addPreferenceCard(content)
        addSection(content, "নিরাপত্তা ও লক • SECURITY & PIN")
        addWideItem(content, "অ্যাপ পিন লক (PIN Lock)", "Protect your ledger with a PIN", R.drawable.ic_lock) { SetPinActivity::class.java }
        addSection(content, "অ্যাপ ও ডেভেলপার • ABOUT")
        addWideItem(content, "Khalequzzaman", "App Architect & Developer", R.drawable.ic_info) { AboutActivity::class.java }
        setContentView(root)
    }

    private fun profileCard(): MaterialCardView = MaterialCardView(this).apply {
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated))
        strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle)
        strokeWidth = dp(1)
        radius = dp(20).toFloat()
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)) }
        row.addView(TextView(context).apply { text = "K"; gravity = Gravity.CENTER; setTextColor(primary); textSize = 20f; setTypeface(typeface, Typeface.BOLD); setBackgroundResource(R.drawable.bg_chip_selected) }, LinearLayout.LayoutParams(dp(48), dp(48)))
        row.addView(TextView(context).apply { text = "Tanvir Ahmed\nMFS & wallet sync active"; setTextColor(ContextCompat.getColor(context, R.color.text_primary)); textSize = 14f; setPadding(dp(12), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))
        addView(row)
    }

    private fun addSection(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply { text = title; setTextColor(ContextCompat.getColor(context, R.color.text_secondary)); textSize = 13f; setTypeface(typeface, Typeface.BOLD); setPadding(dp(2), dp(18), dp(2), dp(8)) })
    }

    private fun addItem(parent: LinearLayout, title: String, subtitle: String, icon: Int, target: () -> Class<*>) {
        val item = MaterialCardView(this).apply { setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated)); strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle); strokeWidth = dp(1); radius = dp(16).toFloat(); setOnClickListener { startActivity(Intent(this@SettingsActivity, target())) } }
        val body = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)) }
        body.addView(TextView(this).apply { text = "✦"; gravity = Gravity.CENTER; setTextColor(primary); textSize = 20f }, LinearLayout.LayoutParams(dp(36), dp(36)))
        body.addView(TextView(this).apply { text = "$title\n$subtitle"; setTextColor(ContextCompat.getColor(context, R.color.text_primary)); textSize = 13f; setPadding(dp(10), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))
        body.addView(TextView(this).apply { text = "›"; setTextColor(muted); textSize = 24f })
        item.addView(body)
        parent.addView(item, LinearLayout.LayoutParams(-1, dp(82)).apply { bottomMargin = dp(8) })
    }

    private fun addWideItem(parent: LinearLayout, title: String, subtitle: String, icon: Int, target: () -> Class<*>) {
        val row = MaterialCardView(this).apply { setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated)); strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle); strokeWidth = dp(1); radius = dp(16).toFloat(); setOnClickListener { startActivity(Intent(this@SettingsActivity, target())) } }
        val body = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)) }
        body.addView(TextView(this).apply { text = "▣"; setTextColor(primary); textSize = 20f }, LinearLayout.LayoutParams(dp(36), dp(36)))
        body.addView(TextView(this).apply { text = "$title\n$subtitle"; setTextColor(ContextCompat.getColor(context, R.color.text_primary)); textSize = 14f; setPadding(dp(10), 0, 0, 0) }, LinearLayout.LayoutParams(0, -2, 1f))
        body.addView(TextView(this).apply { text = "›"; setTextColor(muted); textSize = 24f })
        row.addView(body)
        parent.addView(row, LinearLayout.LayoutParams(-1, dp(68)).apply { bottomMargin = dp(8) })
    }

    private fun addPreferenceCard(parent: LinearLayout) {
        val card = MaterialCardView(this).apply { setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated)); strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle); strokeWidth = dp(1); radius = dp(16).toFloat() }
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        listOf("ভাষা / App Language       বাংলা  |  English", "মুদ্রা / Primary Currency       ৳ BDT", "ডার্ক মোড / Dark Theme       ✓").forEach { value -> body.addView(TextView(this).apply { text = value; setTextColor(ContextCompat.getColor(context, R.color.text_primary)); textSize = 13f; setPadding(0, dp(12), 0, dp(12)) }) }
        card.addView(body)
        parent.addView(card)
    }
}

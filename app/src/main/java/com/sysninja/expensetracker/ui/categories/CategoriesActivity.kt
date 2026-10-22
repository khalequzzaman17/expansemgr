package com.sysninja.expensetracker.ui.categories

import android.content.Intent
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.ui.dp
import com.sysninja.expensetracker.ui.stitchToolbar
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {
    private val primary by lazy { ContextCompat.getColor(this, R.color.teal_primary) }
    private val textPrimary by lazy { ContextCompat.getColor(this, R.color.text_primary) }
    private val muted by lazy { ContextCompat.getColor(this, R.color.text_muted) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.bg_navy_dark))
        }
        root.addView(stitchToolbar("Categories", "হিসাব খাতা • Ledger", ::finish))

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(24))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val titleRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        titleRow.addView(TextView(this).apply {
            text = "ক্যাটাগরি সমূহ\nCategories & Tags Management"
            setTextColor(textPrimary)
            textSize = 20f
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        titleRow.addView(MaterialButton(this).apply {
            text = "+  নতুন খাত"
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(context, R.color.text_inverse))
            setOnClickListener { startActivity(Intent(this@CategoriesActivity, AddEditCategoryActivity::class.java)) }
        }, LinearLayout.LayoutParams(dp(132), dp(48)))
        content.addView(titleRow)

        val tabs = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_card_rounded)
            setPadding(dp(6), dp(6), dp(6), dp(6))
        }
        tabs.addView(TextView(this).apply { text = "↓  ব্যয় খাত (১৬)"; setTextColor(primary); textSize = 14f; setPadding(dp(12), dp(10), dp(12), dp(10)) }, LinearLayout.LayoutParams(0, -2, 1f))
        tabs.addView(TextView(this).apply { text = "↑  আয় খাত (৬)"; setTextColor(ContextCompat.getColor(context, R.color.text_secondary)); textSize = 14f; setPadding(dp(12), dp(10), dp(12), dp(10)) }, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(tabs, LinearLayout.LayoutParams(-1, dp(58)).apply { topMargin = dp(16) })

        content.addView(EditText(this).apply {
            hint = "⌕  খাত বা ট্যাগ খুঁজুন... (Search category)"
            setHintTextColor(muted)
            setTextColor(textPrimary)
            textSize = 14f
            setSingleLine()
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), 0, dp(16), 0)
        }, LinearLayout.LayoutParams(-1, dp(54)).apply { topMargin = dp(16) })

        content.addView(TextView(this).apply {
            text = "ⓘ  মোট ২৮টি সক্রিয় খাত • টাইপ করে ফিল্টার করুন"
            setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            textSize = 12f
            setPadding(dp(4), dp(12), dp(4), dp(10))
        })

        val listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(listContainer)
        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            app.repository.getAllActiveCategories().collect { categories ->
                listContainer.removeAllViews()
                categories.forEach { category ->
                    listContainer.addView(categoryCard(category.name, category.type))
                }
            }
        }
    }

    private fun categoryCard(name: String, type: String): MaterialCardView = MaterialCardView(this).apply {
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_navy_elevated))
        strokeColor = ContextCompat.getColor(context, R.color.stroke_subtle)
        strokeWidth = dp(1)
        radius = dp(18).toFloat()
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)) }
        row.addView(TextView(context).apply {
            text = if (type.equals("INCOME", true)) "↑" else "⌁"
            gravity = Gravity.CENTER
            setTextColor(primary)
            textSize = 22f
            setBackgroundResource(R.drawable.bg_chip_selected)
        }, LinearLayout.LayoutParams(dp(48), dp(48)))
        row.addView(TextView(context).apply {
            text = "$name\n${if (type.equals("INCOME", true)) "Income" else "Expense"} category"
            setTextColor(textPrimary)
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(12), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(TextView(context).apply {
            text = "•  ০ লেনদেন\n⋮"
            gravity = Gravity.END
            setTextColor(muted)
            textSize = 12f
        })
        addView(row)
        layoutParams = LinearLayout.LayoutParams(-1, dp(82)).apply { bottomMargin = dp(10) }
    }
}

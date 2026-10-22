package com.sysninja.expensetracker.ui.budgets

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.domain.DateUtils
import com.sysninja.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BudgetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@BudgetsActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.budgets_title)
            setTitleTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@BudgetsActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@BudgetsActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val addBtn = MaterialButton(this).apply {
            text = getString(R.string.add_budget)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(32, 24, 32, 16) }
            setOnClickListener {
                startActivity(Intent(this@BudgetsActivity, AddEditBudgetActivity::class.java))
            }
        }
        root.addView(addBtn)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 32)
        }
        scrollView.addView(listContainer)
        root.addView(scrollView)

        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            val monthKey = DateUtils.toMonthKey(System.currentTimeMillis())
            app.repository.getBudgetsForMonth(monthKey).collect { budgets ->
                listContainer.removeAllViews()
                if (budgets.isEmpty()) {
                    val emptyTv = TextView(this@BudgetsActivity).apply {
                        text = "No budgets set for this month"
                        setTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.text_muted))
                        textSize = 14f
                        setPadding(0, 32, 0, 0)
                    }
                    listContainer.addView(emptyTv)
                } else {
                    budgets.forEach { b ->
                        val card = LinearLayout(this@BudgetsActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(32, 28, 32, 28)
                            setBackgroundResource(R.drawable.bg_card_rounded)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 16 }
                        }

                        val nameTv = TextView(this@BudgetsActivity).apply {
                            text = "${b.categoryName} (${b.usedPercentage}%)"
                            setTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.text_primary))
                            textSize = 15f
                            paint.isFakeBoldText = true
                        }
                        card.addView(nameTv)

                        val detailsTv = TextView(this@BudgetsActivity).apply {
                            text = "${getString(R.string.spent)}: ${MoneyUtils.formatCents(b.spentCents, currency)} / Limit: ${MoneyUtils.formatCents(b.limitCents, currency)}"
                            setTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.text_secondary))
                            textSize = 13f
                            setPadding(0, 8, 0, 0)
                        }
                        card.addView(detailsTv)

                        listContainer.addView(card)
                    }
                }
            }
        }
    }
}

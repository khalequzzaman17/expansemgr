package com.expensetracker.ui.debts

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.R
import com.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DebtsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@DebtsActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.debts_title)
            setTitleTextColor(ContextCompat.getColor(this@DebtsActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@DebtsActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@DebtsActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val addBtn = MaterialButton(this).apply {
            text = getString(R.string.add_debt)
            setBackgroundColor(ContextCompat.getColor(this@DebtsActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@DebtsActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(32, 24, 32, 16) }
            setOnClickListener {
                startActivity(Intent(this@DebtsActivity, AddEditDebtActivity::class.java))
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
            app.repository.getAllDebts().collect { debts ->
                listContainer.removeAllViews()
                if (debts.isEmpty()) {
                    val emptyTv = TextView(this@DebtsActivity).apply {
                        text = "No active debts or loans"
                        setTextColor(ContextCompat.getColor(this@DebtsActivity, R.color.text_muted))
                        textSize = 14f
                        setPadding(0, 32, 0, 0)
                    }
                    listContainer.addView(emptyTv)
                } else {
                    debts.forEach { debt ->
                        val card = LinearLayout(this@DebtsActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(32, 28, 32, 28)
                            setBackgroundResource(R.drawable.bg_card_rounded)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 16 }
                            setOnClickListener {
                                startActivity(Intent(this@DebtsActivity, DebtDetailActivity::class.java).apply {
                                    putExtra("EXTRA_DEBT_ID", debt.id)
                                })
                            }
                        }

                        val nameTv = TextView(this@DebtsActivity).apply {
                            text = "${debt.personName} (${debt.type})"
                            setTextColor(ContextCompat.getColor(this@DebtsActivity, R.color.text_primary))
                            textSize = 15f
                            paint.isFakeBoldText = true
                        }
                        card.addView(nameTv)

                        val detailsTv = TextView(this@DebtsActivity).apply {
                            text = "Remaining: ${MoneyUtils.formatCents(debt.outstandingBalanceCents, currency)}"
                            setTextColor(ContextCompat.getColor(this@DebtsActivity, if (debt.type == "PAYABLE") R.color.expense_red else R.color.income_green))
                            textSize = 14f
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

package com.expensetracker.ui.savings

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

class SavingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@SavingsActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.savings_title)
            setTitleTextColor(ContextCompat.getColor(this@SavingsActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@SavingsActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@SavingsActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val addBtn = MaterialButton(this).apply {
            text = getString(R.string.add_savings_goal)
            setBackgroundColor(ContextCompat.getColor(this@SavingsActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@SavingsActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(32, 24, 32, 16) }
            setOnClickListener {
                startActivity(Intent(this@SavingsActivity, AddEditSavingsGoalActivity::class.java))
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
            app.repository.getAllSavingsGoals().collect { goals ->
                listContainer.removeAllViews()
                if (goals.isEmpty()) {
                    val emptyTv = TextView(this@SavingsActivity).apply {
                        text = "No savings goals yet"
                        setTextColor(ContextCompat.getColor(this@SavingsActivity, R.color.text_muted))
                        textSize = 14f
                        setPadding(0, 32, 0, 0)
                    }
                    listContainer.addView(emptyTv)
                } else {
                    goals.forEach { goal ->
                        val card = LinearLayout(this@SavingsActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(32, 28, 32, 28)
                            setBackgroundResource(R.drawable.bg_card_rounded)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 16 }
                            setOnClickListener {
                                startActivity(Intent(this@SavingsActivity, SavingsDetailActivity::class.java).apply {
                                    putExtra("EXTRA_GOAL_ID", goal.id)
                                })
                            }
                        }

                        val nameTv = TextView(this@SavingsActivity).apply {
                            text = goal.name
                            setTextColor(ContextCompat.getColor(this@SavingsActivity, R.color.text_primary))
                            textSize = 16f
                            paint.isFakeBoldText = true
                        }
                        card.addView(nameTv)

                        val detailsTv = TextView(this@SavingsActivity).apply {
                            text = "${MoneyUtils.formatCents(goal.currentAmountCents, currency)} / ${MoneyUtils.formatCents(goal.targetAmountCents, currency)}"
                            setTextColor(ContextCompat.getColor(this@SavingsActivity, R.color.teal_light))
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

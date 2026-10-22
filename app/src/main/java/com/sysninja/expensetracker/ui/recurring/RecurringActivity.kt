package com.sysninja.expensetracker.ui.recurring

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
import com.sysninja.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecurringActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@RecurringActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.recurring_title)
            setTitleTextColor(ContextCompat.getColor(this@RecurringActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@RecurringActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@RecurringActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val addBtn = MaterialButton(this).apply {
            text = getString(R.string.add_recurring)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@RecurringActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(32, 24, 32, 16) }
            setOnClickListener {
                startActivity(Intent(this@RecurringActivity, AddEditRecurringActivity::class.java))
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
            app.repository.getAllRecurring().collect { list ->
                listContainer.removeAllViews()
                if (list.isEmpty()) {
                    val emptyTv = TextView(this@RecurringActivity).apply {
                        text = "No recurring payments scheduled"
                        setTextColor(ContextCompat.getColor(this@RecurringActivity, R.color.text_muted))
                        textSize = 14f
                        setPadding(0, 32, 0, 0)
                    }
                    listContainer.addView(emptyTv)
                } else {
                    list.forEach { item ->
                        val card = LinearLayout(this@RecurringActivity).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(32, 28, 32, 28)
                            setBackgroundResource(R.drawable.bg_card_rounded)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { bottomMargin = 16 }
                        }

                        val nameTv = TextView(this@RecurringActivity).apply {
                            text = "${item.title} (${item.frequency})"
                            setTextColor(ContextCompat.getColor(this@RecurringActivity, R.color.text_primary))
                            textSize = 15f
                            paint.isFakeBoldText = true
                        }
                        card.addView(nameTv)

                        val detailsTv = TextView(this@RecurringActivity).apply {
                            text = MoneyUtils.formatCents(item.amountCents, currency)
                            setTextColor(ContextCompat.getColor(this@RecurringActivity, R.color.teal_light))
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

package com.sysninja.expensetracker.ui.debts

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

class DebtDetailActivity : AppCompatActivity() {

    private var debtId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debtId = intent.getLongExtra("EXTRA_DEBT_ID", 0L)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.debts_title)
            setTitleTextColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@DebtDetailActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val titleTv = TextView(this).apply {
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.text_primary))
            paint.isFakeBoldText = true
        }
        content.addView(titleTv)

        val amountTv = TextView(this).apply {
            textSize = 15f
            setTextColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.text_secondary))
            setPadding(0, 12, 0, 24)
        }
        content.addView(amountTv)

        val repayEt = EditText(this).apply {
            hint = "Repayment amount"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        content.addView(repayEt)

        val repayBtn = MaterialButton(this).apply {
            text = getString(R.string.record_repayment)
            setBackgroundColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@DebtDetailActivity, R.color.text_inverse))
            setOnClickListener {
                val amt = MoneyUtils.parseToCents(repayEt.text.toString())
                if (amt <= 0L) return@setOnClickListener

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val debt = app.repository.getDebtById(debtId) ?: return@launch
                    val newRemaining = (debt.outstandingBalanceCents - amt).coerceAtLeast(0L)
                    val newStatus = if (newRemaining == 0L) "PAID" else "PENDING"
                    app.repository.updateDebt(debt.copy(outstandingBalanceCents = newRemaining, status = newStatus))
                    Toast.makeText(this@DebtDetailActivity, "Repayment saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        content.addView(repayBtn)

        root.addView(content)
        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            val debt = app.repository.getDebtById(debtId) ?: return@launch
            titleTv.text = "${debt.personName} (${debt.type})"
            amountTv.text = "Remaining: ${MoneyUtils.formatCents(debt.outstandingBalanceCents, currency)} / Total: ${MoneyUtils.formatCents(debt.originalAmountCents, currency)}"
        }
    }
}

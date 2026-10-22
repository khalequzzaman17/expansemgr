package com.sysninja.expensetracker.ui.transactions

import com.sysninja.expensetracker.ui.dp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.domain.DateUtils
import com.sysninja.expensetracker.domain.MoneyUtils
import com.sysninja.expensetracker.ui.addtransaction.AddEditTransactionActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionDetailActivity : AppCompatActivity() {

    private var transactionId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionId = intent.getLongExtra("EXTRA_TRANSACTION_ID", 0L)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.transaction_details)
            setTitleTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@TransactionDetailActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val titleTv = TextView(this).apply {
            textSize = 22f
            setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.text_primary))
            paint.isFakeBoldText = true
        }
        content.addView(titleTv)

        val amountTv = TextView(this).apply {
            textSize = 28f
            paint.isFakeBoldText = true
            setPadding(0, 16, 0, 16)
        }
        content.addView(amountTv)

        val dateTv = TextView(this).apply {
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.text_secondary))
            setPadding(0, 0, 0, 24)
        }
        content.addView(dateTv)

        val noteTv = TextView(this).apply {
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.text_secondary))
            setPadding(0, 0, 0, 32)
        }
        content.addView(noteTv)

        val editBtn = MaterialButton(this).apply {
            text = getString(R.string.edit)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.text_inverse))
            setOnClickListener {
                startActivity(Intent(this@TransactionDetailActivity, AddEditTransactionActivity::class.java).apply {
                    putExtra("EXTRA_TRANSACTION_ID", transactionId)
                })
                finish()
            }
        }
        content.addView(editBtn)

        val deleteBtn = MaterialButton(this).apply {
            text = getString(R.string.delete)
            setBackgroundColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.expense_red_bg))
            setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.expense_red))
            setOnClickListener {
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    app.repository.deleteTransactionById(transactionId)
                    Toast.makeText(this@TransactionDetailActivity, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        content.addView(deleteBtn)

        root.addView(content)
        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            val txWithDetails = app.repository.getTransactionWithDetailsById(transactionId).first()
            if (txWithDetails == null) {
                finish()
                return@launch
            }
            val tx = txWithDetails.transaction
            titleTv.text = tx.title
            dateTv.text = DateUtils.formatDateTime(tx.timestamp)
            noteTv.text = tx.note ?: "No notes"

            val amt = MoneyUtils.formatCents(tx.amountCents, currency)
            when (tx.type) {
                "INCOME" -> {
                    amountTv.text = "+ $amt"
                    amountTv.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.income_green))
                }
                "EXPENSE" -> {
                    amountTv.text = "- $amt"
                    amountTv.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.expense_red))
                }
                else -> {
                    amountTv.text = amt
                    amountTv.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.transfer_blue))
                }
            }
        }
    }
}

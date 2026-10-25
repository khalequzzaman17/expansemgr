package com.sysninja.expensetracker.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityTransactionDetailBinding
import com.sysninja.expensetracker.domain.DateUtils
import com.sysninja.expensetracker.domain.MoneyUtils
import com.sysninja.expensetracker.ui.addtransaction.AddEditTransactionActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionDetailBinding
    private var transactionId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getLongExtra("EXTRA_TRANSACTION_ID", 0L)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            startActivity(Intent(this, AddEditTransactionActivity::class.java).apply {
                putExtra("EXTRA_TRANSACTION_ID", transactionId)
            })
            finish()
        }

        binding.btnDelete.setOnClickListener {
            val app = application as ExpenseTrackerApp
            lifecycleScope.launch {
                app.repository.deleteTransactionById(transactionId)
                Toast.makeText(this@TransactionDetailActivity, getString(R.string.tx_deleted_success), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        loadTransactionDetails()
    }

    private fun loadTransactionDetails() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            val txWithDetails = app.repository.getTransactionWithDetailsById(transactionId).first()
            if (txWithDetails == null) {
                finish()
                return@launch
            }

            val tx = txWithDetails.transaction
            binding.tvTitle.text = tx.title
            binding.tvDate.text = DateUtils.formatDateTime(tx.timestamp)

            val note = tx.note
            if (!note.isNullOrBlank()) {
                binding.layoutNote.visibility = View.VISIBLE
                binding.tvNote.text = note
            } else {
                binding.layoutNote.visibility = View.GONE
            }

            val catName = txWithDetails.category?.name ?: getString(R.string.default_general_category)
            binding.tvCategory.text = catName

            val fromAcc = txWithDetails.account?.name ?: getString(R.string.default_account_label)
            val toAcc = txWithDetails.toAccount?.name
            binding.tvAccount.text = if (toAcc != null) "$fromAcc → $toAcc" else fromAcc

            val amt = MoneyUtils.formatCents(tx.amountCents, currency)
            when (tx.type) {
                "INCOME" -> {
                    binding.tvAmount.text = "+ $amt"
                    binding.tvAmount.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.secondary_mint))
                    binding.tvType.text = getString(R.string.type_income)
                    binding.tvStatusChip.text = getString(R.string.tx_status_credited)
                    binding.ivCategoryAvatarBg.setBackgroundResource(R.drawable.bg_badge_cash)
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_income)
                    binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(this@TransactionDetailActivity, R.color.secondary_mint))
                }
                "EXPENSE" -> {
                    binding.tvAmount.text = "- $amt"
                    binding.tvAmount.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.expense_red))
                    binding.tvType.text = getString(R.string.type_expense)
                    binding.tvStatusChip.text = getString(R.string.tx_status_completed)
                    binding.ivCategoryAvatarBg.setBackgroundResource(R.drawable.bg_badge_expense)
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_expense)
                    binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(this@TransactionDetailActivity, R.color.expense_red))
                }
                else -> {
                    binding.tvAmount.text = "⇄ $amt"
                    binding.tvAmount.setTextColor(ContextCompat.getColor(this@TransactionDetailActivity, R.color.teal_primary))
                    binding.tvType.text = getString(R.string.type_transfer)
                    binding.tvStatusChip.text = getString(R.string.tx_status_transferred)
                    binding.ivCategoryAvatarBg.setBackgroundResource(R.drawable.bg_badge_bank)
                    binding.ivCategoryIcon.setImageResource(R.drawable.ic_transfer)
                    binding.ivCategoryIcon.setColorFilter(ContextCompat.getColor(this@TransactionDetailActivity, R.color.teal_primary))
                }
            }
        }
    }
}

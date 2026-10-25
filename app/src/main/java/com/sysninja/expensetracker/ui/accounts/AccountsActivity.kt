package com.sysninja.expensetracker.ui.accounts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityAccountsBinding
import com.sysninja.expensetracker.databinding.ItemAccountCardBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AccountsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnAddAccount.setOnClickListener {
            startActivity(Intent(this, AddEditAccountActivity::class.java))
        }

        binding.btnTransfer.setOnClickListener {
            startActivity(Intent(this, TransferActivity::class.java))
        }

        loadAccounts()
    }

    private fun loadAccounts() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            app.repository.getAccountBalances().collect { balances ->
                binding.accountsListContainer.removeAllViews()

                val totalCents = balances.sumOf { it.balanceCents }
                binding.tvHeroTotalBalance.text = MoneyUtils.formatCents(totalCents, currency)
                binding.tvActiveAccountsChip.text = "${balances.size}টি সক্রিয় অ্যাকাউন্ট"

                // Calculate asset allocation
                var bankCents = 0L
                var mfsCents = 0L
                var cashCents = 0L

                balances.forEach { item ->
                    val lower = (item.accountName + " " + item.accountType).lowercase()
                    when {
                        lower.contains("bkash") || lower.contains("বিকাশ") || lower.contains("nagad") || lower.contains("নগদ") || lower.contains("mfs") -> {
                            mfsCents += item.balanceCents.coerceAtLeast(0)
                        }
                        lower.contains("cash") || lower.contains("নগদ") -> {
                            cashCents += item.balanceCents.coerceAtLeast(0)
                        }
                        else -> {
                            bankCents += item.balanceCents.coerceAtLeast(0)
                        }
                    }
                }

                val positiveTotal = (bankCents + mfsCents + cashCents).coerceAtLeast(1L)
                val bankPct = ((bankCents.toDouble() / positiveTotal.toDouble()) * 100).toInt()
                val mfsPct = ((mfsCents.toDouble() / positiveTotal.toDouble()) * 100).toInt()
                val cashPct = (100 - bankPct - mfsPct).coerceAtLeast(0)

                binding.tvPctBank.text = "$bankPct%"
                binding.tvPctMfs.text = "$mfsPct%"
                binding.tvPctCash.text = "$cashPct%"

                val segBankParams = binding.segBank.layoutParams as LinearLayout.LayoutParams
                segBankParams.weight = bankPct.coerceAtLeast(1).toFloat()
                binding.segBank.layoutParams = segBankParams

                val segMfsParams = binding.segMfs.layoutParams as LinearLayout.LayoutParams
                segMfsParams.weight = mfsPct.coerceAtLeast(1).toFloat()
                binding.segMfs.layoutParams = segMfsParams

                val segCashParams = binding.segCash.layoutParams as LinearLayout.LayoutParams
                segCashParams.weight = cashPct.coerceAtLeast(1).toFloat()
                binding.segCash.layoutParams = segCashParams

                // Render account items
                balances.forEach { item ->
                    val itemBinding = ItemAccountCardBinding.inflate(
                        LayoutInflater.from(this@AccountsActivity),
                        binding.accountsListContainer,
                        false
                    )

                    itemBinding.tvAccountName.text = item.accountName
                    itemBinding.tvAccountBalance.text = MoneyUtils.formatCents(item.balanceCents, currency)

                    val lower = (item.accountName + " " + item.accountType).lowercase()
                    when {
                        lower.contains("bkash") || lower.contains("বিকাশ") -> {
                            itemBinding.ivAccountAvatarBg.setBackgroundResource(R.drawable.bg_badge_bkash)
                            itemBinding.tvAvatarLetter.text = "বি"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bkash_text))
                            itemBinding.tvBadgeChip.text = "bKash"
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_bkash)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bkash_text))
                            itemBinding.tvAccountSubtitle.text = "MFS Wallet • bKash"
                        }
                        lower.contains("nagad") || lower.contains("নগদ") -> {
                            itemBinding.ivAccountAvatarBg.setBackgroundResource(R.drawable.bg_badge_nagad)
                            itemBinding.tvAvatarLetter.text = "ন"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.nagad_text))
                            itemBinding.tvBadgeChip.text = "Nagad"
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_nagad)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.nagad_text))
                            itemBinding.tvAccountSubtitle.text = "MFS Wallet • Postal Cash"
                        }
                        lower.contains("cash") -> {
                            itemBinding.ivAccountAvatarBg.setBackgroundResource(R.drawable.bg_badge_cash)
                            itemBinding.tvAvatarLetter.text = "৳"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.cash_text))
                            itemBinding.tvBadgeChip.text = "Cash"
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_cash)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.cash_text))
                            itemBinding.tvAccountSubtitle.text = "Physical Cash • নগদ নোট"
                        }
                        else -> {
                            itemBinding.ivAccountAvatarBg.setBackgroundResource(R.drawable.bg_badge_bank)
                            itemBinding.tvAvatarLetter.text = "বা"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bank_text))
                            itemBinding.tvBadgeChip.text = item.accountType
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_bank)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bank_text))
                            itemBinding.tvAccountSubtitle.text = "Bank Account • ব্যাংক"
                        }
                    }

                    itemBinding.root.setOnClickListener {
                        val intent = Intent(this@AccountsActivity, AddEditAccountActivity::class.java).apply {
                            putExtra("EXTRA_ACCOUNT_ID", item.accountId)
                        }
                        startActivity(intent)
                    }

                    binding.accountsListContainer.addView(itemBinding.root)
                }
            }
        }
    }
}

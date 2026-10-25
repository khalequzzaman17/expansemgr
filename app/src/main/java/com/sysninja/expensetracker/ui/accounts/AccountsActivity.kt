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
import com.sysninja.expensetracker.domain.DateUtils
import com.sysninja.expensetracker.domain.MoneyUtils
import java.util.Locale
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
                binding.tvActiveAccountsChip.text = "${balances.size} ${getString(R.string.active_accounts_label)}"

                // Dynamic MoM Net Worth Growth
                val start = DateUtils.getStartOfMonth()
                val end = DateUtils.getEndOfMonth()
                val income = app.repository.getTotalIncomeInPeriod(start, end).first()
                val expense = app.repository.getTotalExpenseInPeriod(start, end).first()
                val netCash = income - expense
                val startNetWorth = totalCents - netCash
                val growthPct = when {
                    startNetWorth > 0L -> ((netCash.toDouble() / startNetWorth.toDouble()) * 100.0).coerceIn(-999.0, 999.0)
                    startNetWorth == 0L && totalCents > 0L -> 100.0
                    startNetWorth == 0L && totalCents < 0L -> -100.0
                    else -> 0.0
                }
                val growthFormatted = if (growthPct > 0.0) {
                    String.format(Locale.getDefault(), "+%.1f%%", growthPct)
                } else if (growthPct < 0.0) {
                    String.format(Locale.getDefault(), "%.1f%%", growthPct)
                } else {
                    "0.0%"
                }
                binding.tvAccountsGrowth.text = getString(R.string.growth_this_month, growthFormatted)
                if (growthPct >= 0) {
                    binding.tvAccountsGrowth.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.secondary_mint))
                    binding.layoutAccountsGrowth.setBackgroundResource(R.drawable.bg_badge_cash)
                } else {
                    binding.tvAccountsGrowth.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.expense_red))
                    binding.layoutAccountsGrowth.setBackgroundResource(R.drawable.bg_badge_red)
                }

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

                val positiveTotal = bankCents + mfsCents + cashCents
                if (positiveTotal > 0L) {
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

                    binding.tvAssetAllocationStatus.text = getString(R.string.all_categorized)
                } else {
                    binding.tvPctBank.text = "0%"
                    binding.tvPctMfs.text = "0%"
                    binding.tvPctCash.text = "0%"

                    val segBankParams = binding.segBank.layoutParams as LinearLayout.LayoutParams
                    segBankParams.weight = 1f
                    binding.segBank.layoutParams = segBankParams

                    val segMfsParams = binding.segMfs.layoutParams as LinearLayout.LayoutParams
                    segMfsParams.weight = 1f
                    binding.segMfs.layoutParams = segMfsParams

                    val segCashParams = binding.segCash.layoutParams as LinearLayout.LayoutParams
                    segCashParams.weight = 1f
                    binding.segCash.layoutParams = segCashParams

                    binding.tvAssetAllocationStatus.text = getString(R.string.no_funds_allocated)
                }

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
                            itemBinding.tvAvatarLetter.text = "B"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bkash_text))
                            itemBinding.tvBadgeChip.text = "bKash"
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_bkash)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bkash_text))
                            itemBinding.tvAccountSubtitle.text = getString(R.string.pm_bkash)
                        }
                        lower.contains("nagad") || lower.contains("নগদ") -> {
                            itemBinding.ivAccountAvatarBg.setBackgroundResource(R.drawable.bg_badge_nagad)
                            itemBinding.tvAvatarLetter.text = "N"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.nagad_text))
                            itemBinding.tvBadgeChip.text = "Nagad"
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_nagad)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.nagad_text))
                            itemBinding.tvAccountSubtitle.text = getString(R.string.pm_nagad)
                        }
                        lower.contains("cash") -> {
                            itemBinding.ivAccountAvatarBg.setBackgroundResource(R.drawable.bg_badge_cash)
                            itemBinding.tvAvatarLetter.text = "C"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.cash_text))
                            itemBinding.tvBadgeChip.text = getString(R.string.pm_cash)
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_cash)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.cash_text))
                            itemBinding.tvAccountSubtitle.text = getString(R.string.pm_cash)
                        }
                        else -> {
                            itemBinding.ivAccountAvatarBg.setBackgroundResource(R.drawable.bg_badge_bank)
                            itemBinding.tvAvatarLetter.text = "A"
                            itemBinding.tvAvatarLetter.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bank_text))
                            itemBinding.tvBadgeChip.text = item.accountType
                            itemBinding.tvBadgeChip.setBackgroundResource(R.drawable.bg_badge_bank)
                            itemBinding.tvBadgeChip.setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.bank_text))
                            itemBinding.tvAccountSubtitle.text = getString(R.string.pm_bank)
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

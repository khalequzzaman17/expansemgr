package com.sysninja.expensetracker.ui.budgets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityBudgetsBinding
import com.sysninja.expensetracker.databinding.ItemBudgetCardBinding
import com.sysninja.expensetracker.domain.DateUtils
import com.sysninja.expensetracker.domain.MoneyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BudgetsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnAddBudget.setOnClickListener {
            startActivity(Intent(this, AddEditBudgetActivity::class.java))
        }

        loadBudgets()
    }

    private fun loadBudgets() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            val monthKey = DateUtils.toMonthKey(System.currentTimeMillis())

            app.repository.getBudgetsForMonth(monthKey).collect { budgets ->
                binding.budgetsListContainer.removeAllViews()

                if (budgets.isEmpty()) {
                    binding.tvEmptyBudgets.visibility = View.VISIBLE
                    binding.tvBudgetsCount.text = getString(R.string.goals_count_format, 0)
                    binding.tvTotalSpentHero.text = MoneyUtils.formatCents(0, currency)
                    binding.tvSafeLeftHero.text = MoneyUtils.formatCents(0, currency)
                    binding.progressMaster.progress = 0
                    binding.tvMasterPercentBadge.text = "0%"
                    binding.tvDailySafeLimit.text = "${MoneyUtils.formatCents(0, currency)} / ${getString(R.string.freq_daily)}"
                } else {
                    binding.tvEmptyBudgets.visibility = View.GONE
                    binding.tvBudgetsCount.text = getString(R.string.goals_count_format, budgets.size)

                    val totalLimit = budgets.sumOf { it.limitCents }
                    val totalSpent = budgets.sumOf { it.spentCents }
                    val totalRemaining = (totalLimit - totalSpent).coerceAtLeast(0L)
                    val overallPct = if (totalLimit > 0) ((totalSpent.toDouble() / totalLimit.toDouble()) * 100).toInt() else 0

                    binding.tvTotalSpentHero.text = MoneyUtils.formatCents(totalSpent, currency)
                    binding.tvSafeLeftHero.text = MoneyUtils.formatCents(totalRemaining, currency)
                    binding.progressMaster.progress = overallPct.coerceIn(0, 100)
                    binding.tvMasterPercentBadge.text = "$overallPct%"
                    binding.tvMasterCapSubtitle.text = "${getString(R.string.master_monthly_cap)} • ${MoneyUtils.formatCents(totalLimit, currency)}"

                    val daysRemainingInMonth = 8 // approximate days
                    val dailySafeCents = if (daysRemainingInMonth > 0) totalRemaining / daysRemainingInMonth else 0L
                    binding.tvDailySafeLimit.text = "${MoneyUtils.formatCents(dailySafeCents, currency)} / ${getString(R.string.freq_daily)}"

                    budgets.forEach { b ->
                        val itemBinding = ItemBudgetCardBinding.inflate(
                            LayoutInflater.from(this@BudgetsActivity),
                            binding.budgetsListContainer,
                            false
                        )

                        itemBinding.tvCategoryName.text = b.categoryName
                        itemBinding.tvSpentAmount.text = MoneyUtils.formatCents(b.spentCents, currency)
                        itemBinding.tvBudgetLimit.text = "${getString(R.string.budgets_title)}: ${MoneyUtils.formatCents(b.limitCents, currency)}"
                        itemBinding.progressCategory.progress = b.usedPercentage.coerceIn(0, 100)

                        val remCents = (b.limitCents - b.spentCents).coerceAtLeast(0L)
                        itemBinding.tvRemainingAmount.text = getString(R.string.remaining_amount_format, MoneyUtils.formatCents(remCents, currency))

                        when {
                            b.usedPercentage >= 100 -> {
                                itemBinding.tvStatusChip.text = "${getString(R.string.over_budget)} (${b.usedPercentage}%)"
                                itemBinding.tvStatusChip.setBackgroundResource(R.drawable.bg_badge_expense)
                                itemBinding.tvStatusChip.setTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.expense_red))
                                itemBinding.progressCategory.setIndicatorColor(ContextCompat.getColor(this@BudgetsActivity, R.color.expense_red))
                            }
                            b.usedPercentage >= 85 -> {
                                itemBinding.tvStatusChip.text = getString(R.string.budget_warning, b.usedPercentage)
                                itemBinding.tvStatusChip.setBackgroundResource(R.drawable.bg_badge_nagad)
                                itemBinding.tvStatusChip.setTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.debt_orange))
                                itemBinding.progressCategory.setIndicatorColor(ContextCompat.getColor(this@BudgetsActivity, R.color.debt_orange))
                            }
                            else -> {
                                itemBinding.tvStatusChip.text = "${getString(R.string.status_active)} (${b.usedPercentage}%)"
                                itemBinding.tvStatusChip.setBackgroundResource(R.drawable.bg_badge_cash)
                                itemBinding.tvStatusChip.setTextColor(ContextCompat.getColor(this@BudgetsActivity, R.color.secondary_mint))
                                itemBinding.progressCategory.setIndicatorColor(ContextCompat.getColor(this@BudgetsActivity, R.color.secondary_mint))
                            }
                        }

                        itemBinding.btnEditBudget.setOnClickListener {
                            val intent = Intent(this@BudgetsActivity, AddEditBudgetActivity::class.java).apply {
                                putExtra("EXTRA_BUDGET_ID", b.budgetId)
                            }
                            startActivity(intent)
                        }

                        binding.budgetsListContainer.addView(itemBinding.root)
                    }
                }
            }
        }
    }
}

package com.expensetracker.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.expensetracker.R
import com.expensetracker.data.model.PeriodType
import com.expensetracker.databinding.FragmentDashboardBinding
import com.expensetracker.domain.MoneyUtils
import com.expensetracker.ui.addtransaction.AddEditTransactionActivity
import com.expensetracker.ui.main.MainActivity
import com.expensetracker.ui.settings.SettingsActivity
import com.expensetracker.ui.transactions.TransactionListAdapter
import com.expensetracker.ui.views.DonutChartView
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeUiState()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionListAdapter("৳") { item ->
            val intent = Intent(requireContext(), AddEditTransactionActivity::class.java).apply {
                putExtra("EXTRA_TRANSACTION_ID", item.transaction.id)
            }
            startActivity(intent)
        }

        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun setupListeners() {
        binding.ivSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnQuickExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditTransactionActivity::class.java).apply {
                putExtra("EXTRA_TYPE", "EXPENSE")
            })
        }

        binding.btnQuickIncome.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditTransactionActivity::class.java).apply {
                putExtra("EXTRA_TYPE", "INCOME")
            })
        }

        binding.btnQuickTransfer.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditTransactionActivity::class.java).apply {
                putExtra("EXTRA_TYPE", "TRANSFER")
            })
        }

        binding.tvSeeAllTransactions.setOnClickListener {
            (activity as? MainActivity)?.selectTransactionsTab()
        }

        binding.chipGroupPeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            val period = when {
                checkedIds.contains(R.id.chipToday) -> PeriodType.TODAY
                checkedIds.contains(R.id.chipThisWeek) -> PeriodType.THIS_WEEK
                checkedIds.contains(R.id.chipThisMonth) -> PeriodType.THIS_MONTH
                checkedIds.contains(R.id.chipLastMonth) -> PeriodType.LAST_MONTH
                checkedIds.contains(R.id.chipThisYear) -> PeriodType.THIS_YEAR
                else -> PeriodType.THIS_MONTH
            }
            viewModel.selectPeriod(period)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: DashboardUiState) {
        val currency = state.currencySymbol
        transactionAdapter.updateCurrency(currency)

        // Summary cards
        binding.tvNetWorth.text = MoneyUtils.formatCents(state.totalNetWorth, currency)
        binding.tvTotalIncome.text = "+ ${MoneyUtils.formatCents(state.totalIncome, currency)}"
        binding.tvTotalExpense.text = "- ${MoneyUtils.formatCents(state.totalExpense, currency)}"

        val cashFlowPrefix = if (state.netCashFlow >= 0) "+ " else ""
        binding.tvNetCashFlow.text = "$cashFlowPrefix${MoneyUtils.formatCents(state.netCashFlow, currency)}"

        val accountCount = state.accountBalances.size
        binding.tvAccountsCount.text = getString(R.string.accounts_summary) + " ($accountCount)"

        // Category breakdown donut chart
        if (state.categorySpending.isNotEmpty()) {
            binding.cardCategoryBreakdown.visibility = View.VISIBLE
            val slices = state.categorySpending.map { cat ->
                val color = try {
                    Color.parseColor(cat.categoryColor)
                } catch (e: Exception) {
                    Color.parseColor("#10B981")
                }
                DonutChartView.Slice(cat.categoryName, cat.totalAmountCents.toDouble(), color)
            }
            binding.donutChart.setData(
                slices,
                MoneyUtils.formatCents(state.totalExpense, currency),
                "Expenses"
            )

            // Top categories list
            binding.layoutTopCategories.removeAllViews()
            state.categorySpending.take(4).forEach { cat ->
                val row = TextView(requireContext()).apply {
                    val amountStr = MoneyUtils.formatCents(cat.totalAmountCents, currency)
                    val percentStr = "(${cat.percentage.toInt()}%)"
                    text = "${cat.categoryName} • $percentStr: $amountStr"
                    setTextColor(Color.parseColor("#94A3B8"))
                    textSize = 12f
                    setPadding(0, 4, 0, 4)
                }
                binding.layoutTopCategories.addView(row)
            }
        } else {
            binding.cardCategoryBreakdown.visibility = View.GONE
        }

        // Budget overview
        if (state.budgetProgress.isNotEmpty()) {
            binding.cardBudgetProgress.visibility = View.VISIBLE
            val totalLimit = state.budgetProgress.sumOf { it.limitCents }
            val totalSpent = state.budgetProgress.sumOf { it.spentCents }
            val usedPct = if (totalLimit > 0) ((totalSpent.toDouble() / totalLimit.toDouble()) * 100).toInt() else 0
            val remaining = (totalLimit - totalSpent).coerceAtLeast(0L)

            binding.progressBudget.progress = usedPct.coerceIn(0, 100)
            binding.tvBudgetStatus.text = "$usedPct% Used"
            binding.tvBudgetSpent.text = "${getString(R.string.spent)}: ${MoneyUtils.formatCents(totalSpent, currency)}"
            binding.tvBudgetRemaining.text = "${getString(R.string.remaining)}: ${MoneyUtils.formatCents(remaining, currency)}"
        } else {
            binding.cardBudgetProgress.visibility = View.GONE
        }

        // Recent transactions list
        if (state.recentTransactions.isNotEmpty()) {
            binding.rvRecentTransactions.visibility = View.VISIBLE
            binding.tvNoRecentTransactions.visibility = View.GONE
            transactionAdapter.submitList(state.recentTransactions)
        } else {
            binding.rvRecentTransactions.visibility = View.GONE
            binding.tvNoRecentTransactions.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

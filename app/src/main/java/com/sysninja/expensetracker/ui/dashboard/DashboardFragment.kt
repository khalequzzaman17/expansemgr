package com.sysninja.expensetracker.ui.dashboard

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
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.model.PeriodType
import com.sysninja.expensetracker.databinding.FragmentDashboardBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import com.sysninja.expensetracker.ui.addtransaction.AddEditTransactionActivity
import com.sysninja.expensetracker.ui.main.MainActivity
import com.sysninja.expensetracker.ui.settings.SettingsActivity
import com.sysninja.expensetracker.ui.transactions.TransactionListAdapter
import com.sysninja.expensetracker.ui.views.DonutChartView
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionListAdapter
    private var isBalanceVisible = true
    private var currentNetWorthText = "৳ 0.00"

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
        binding.btnToggleBalance.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            if (isBalanceVisible) {
                binding.btnToggleBalance.setImageResource(R.drawable.ic_visibility)
                binding.tvNetWorth.text = currentNetWorthText
            } else {
                binding.btnToggleBalance.setImageResource(R.drawable.ic_visibility_off)
                binding.tvNetWorth.text = "৳ •••••••"
            }
        }

        binding.btnQuickBudget.setOnClickListener {
            startActivity(Intent(requireContext(), com.sysninja.expensetracker.ui.budgets.BudgetsActivity::class.java))
        }

        binding.btnAnalysis.setOnClickListener {
            startActivity(Intent(requireContext(), com.sysninja.expensetracker.ui.reports.ReportsActivity::class.java))
        }

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

        binding.btnLangToggle.text = com.sysninja.expensetracker.domain.LocaleManager.getToggleLabel(requireContext())
        binding.btnLangToggle.setOnClickListener {
            com.sysninja.expensetracker.domain.LocaleManager.toggleLanguage(requireActivity()) { newLang ->
                val msg = if (newLang == "bn") "ভাষা বাংলা নির্বাচন করা হয়েছে" else "Language switched to English"
                android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        val app = requireContext().applicationContext as ExpenseTrackerApp
        viewLifecycleOwner.lifecycleScope.launch {
            app.preferencesManager.userNameFlow.collect { name ->
                binding.tvUserName.text = name.ifBlank { getString(R.string.default_user_name) }
            }
        }

        binding.tvUserName.setOnClickListener {
            showEditNameDialog()
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
        currentNetWorthText = MoneyUtils.formatCents(state.totalNetWorth, currency)
        if (isBalanceVisible) {
            binding.tvNetWorth.text = currentNetWorthText
        } else {
            binding.tvNetWorth.text = "৳ •••••••"
        }
        binding.tvTotalIncome.text = "+ ${MoneyUtils.formatCents(state.totalIncome, currency)}"
        binding.tvTotalExpense.text = "- ${MoneyUtils.formatCents(state.totalExpense, currency)}"

        val cashFlowPrefix = if (state.netCashFlow >= 0) "+ " else ""
        binding.tvNetCashFlow.text = "$cashFlowPrefix${MoneyUtils.formatCents(state.netCashFlow, currency)}"

        val savingsPct = if (state.totalIncome > 0) {
            ((state.netCashFlow.toDouble() / state.totalIncome.toDouble()) * 100).coerceAtLeast(0.0).toInt()
        } else 0
        binding.tvSavingsRate.text = getString(R.string.savings_rate_pct_label, savingsPct)

        val accountCount = state.accountBalances.size
        binding.tvAccountsCount.text = getString(R.string.accounts_count, accountCount)

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
                getString(R.string.type_expense)
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
            binding.tvBudgetStatus.text = "$usedPct%"
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

    private fun showEditNameDialog() {
        val app = requireContext().applicationContext as ExpenseTrackerApp
        val container = android.widget.FrameLayout(requireContext()).apply {
            val padH = (24 * resources.displayMetrics.density).toInt()
            val padV = (12 * resources.displayMetrics.density).toInt()
            setPadding(padH, padV, padH, padV / 2)
        }
        val input = android.widget.EditText(requireContext()).apply {
            hint = getString(R.string.enter_name_hint)
            setText(binding.tvUserName.text.toString().takeIf { it != getString(R.string.default_user_name) } ?: "")
            setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_primary))
            setHintTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            val pad = (14 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            isSingleLine = true
        }
        container.addView(input)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_name_title))
            .setView(container)
            .setPositiveButton(getString(R.string.save_name_action)) { dialog, _ ->
                val entered = input.text.toString().trim()
                val finalName = if (entered.isNotBlank()) entered else getString(R.string.default_user_name)
                viewLifecycleOwner.lifecycleScope.launch {
                    app.preferencesManager.setUserName(finalName)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.sysninja.expensetracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.data.model.*
import com.sysninja.expensetracker.domain.DateUtils
import kotlinx.coroutines.flow.*

data class DashboardUiState(
    val period: PeriodType = PeriodType.THIS_MONTH,
    val totalNetWorth: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val netCashFlow: Long = 0L,
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val budgetProgress: List<BudgetProgressItem> = emptyList(),
    val accountBalances: List<AccountBalanceItem> = emptyList(),
    val currencySymbol: String = "৳"
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExpenseTrackerApp
    private val repository = app.repository
    private val preferences = app.preferencesManager

    private val _selectedPeriod = MutableStateFlow(PeriodType.THIS_MONTH)
    val selectedPeriod: StateFlow<PeriodType> = _selectedPeriod.asStateFlow()

    private val periodTimestamps = _selectedPeriod.map { period ->
        val now = System.currentTimeMillis()
        when (period) {
            PeriodType.TODAY -> DateUtils.getStartOfDay(now) to DateUtils.getEndOfDay(now)
            PeriodType.THIS_WEEK -> DateUtils.getStartOfWeek(now) to DateUtils.getEndOfWeek(now)
            PeriodType.THIS_MONTH -> DateUtils.getStartOfMonth(now) to DateUtils.getEndOfMonth(now)
            PeriodType.LAST_MONTH -> DateUtils.getStartOfPreviousMonth(now) to DateUtils.getEndOfPreviousMonth(now)
            PeriodType.THIS_YEAR -> DateUtils.getStartOfYear(now) to DateUtils.getEndOfYear(now)
            PeriodType.CUSTOM -> DateUtils.getStartOfMonth(now) to DateUtils.getEndOfMonth(now)
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        _selectedPeriod,
        periodTimestamps.flatMapLatest { (start, end) ->
            combine(
                repository.getTotalIncomeInPeriod(start, end),
                repository.getTotalExpenseInPeriod(start, end),
                repository.getCategorySpendingInPeriod(start, end)
            ) { inc, exp, cats -> Triple(inc, exp, cats) }
        },
        repository.getRecentTransactionsWithDetails(6),
        repository.getBudgetsForMonth(DateUtils.toMonthKey(System.currentTimeMillis())),
        combine(
            repository.getAccountBalances(),
            preferences.currencySymbolFlow
        ) { accounts, currency -> accounts to currency }
    ) { period, (income, expense, cats), recentTxs, budgets, (accounts, currency) ->
        val netCash = income - expense
        val netWorth = accounts.sumOf { it.balanceCents }

        DashboardUiState(
            period = period,
            totalNetWorth = netWorth,
            totalIncome = income,
            totalExpense = expense,
            netCashFlow = netCash,
            recentTransactions = recentTxs,
            categorySpending = cats,
            budgetProgress = budgets,
            accountBalances = accounts,
            currencySymbol = currency
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )

    fun selectPeriod(period: PeriodType) {
        _selectedPeriod.value = period
    }
}

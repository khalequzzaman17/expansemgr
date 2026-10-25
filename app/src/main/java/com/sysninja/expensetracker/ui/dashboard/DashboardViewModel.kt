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
    val netWorthGrowthPct: Double = 0.0,
    val incomeGrowthPct: Double = 0.0,
    val expenseGrowthPct: Double = 0.0,
    val recentTransactions: List<TransactionWithDetails> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val budgetProgress: List<BudgetProgressItem> = emptyList(),
    val accountBalances: List<AccountBalanceItem> = emptyList(),
    val currencySymbol: String = "৳"
)

private data class CombinedPeriodData(
    val income: Long,
    val expense: Long,
    val categorySpending: List<CategorySpending>,
    val prevIncome: Long,
    val prevExpense: Long
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExpenseTrackerApp
    private val repository = app.repository
    private val preferences = app.preferencesManager

    private val _selectedPeriod = MutableStateFlow(PeriodType.THIS_MONTH)
    val selectedPeriod: StateFlow<PeriodType> = _selectedPeriod.asStateFlow()

    private fun getPreviousPeriodTimestamps(period: PeriodType, start: Long): Pair<Long, Long> {
        return when (period) {
            PeriodType.TODAY -> DateUtils.getStartOfDay(start - 86400000L) to DateUtils.getEndOfDay(start - 86400000L)
            PeriodType.THIS_WEEK -> (start - 7 * 86400000L) to (start - 1L)
            PeriodType.THIS_MONTH, PeriodType.CUSTOM -> DateUtils.getStartOfPreviousMonth(start) to DateUtils.getEndOfPreviousMonth(start)
            PeriodType.LAST_MONTH -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = start; add(java.util.Calendar.MONTH, -1) }
                DateUtils.getStartOfMonth(cal.timeInMillis) to DateUtils.getEndOfMonth(cal.timeInMillis)
            }
            PeriodType.THIS_YEAR -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = start; add(java.util.Calendar.YEAR, -1) }
                DateUtils.getStartOfYear(cal.timeInMillis) to DateUtils.getEndOfYear(cal.timeInMillis)
            }
        }
    }

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
            val (prevStart, prevEnd) = getPreviousPeriodTimestamps(_selectedPeriod.value, start)
            combine(
                repository.getTotalIncomeInPeriod(start, end),
                repository.getTotalExpenseInPeriod(start, end),
                repository.getCategorySpendingInPeriod(start, end),
                repository.getTotalIncomeInPeriod(prevStart, prevEnd),
                repository.getTotalExpenseInPeriod(prevStart, prevEnd)
            ) { inc, exp, cats, prevInc, prevExp ->
                CombinedPeriodData(inc, exp, cats, prevInc, prevExp)
            }
        },
        repository.getRecentTransactionsWithDetails(6),
        repository.getBudgetsForMonth(DateUtils.toMonthKey(System.currentTimeMillis())),
        combine(
            repository.getAccountBalances(),
            preferences.currencySymbolFlow
        ) { accounts, currency -> accounts to currency }
    ) { period, periodData, recentTxs, budgets, (accounts, currency) ->
        val income = periodData.income
        val expense = periodData.expense
        val cats = periodData.categorySpending
        val prevInc = periodData.prevIncome
        val prevExp = periodData.prevExpense

        val netCash = income - expense
        val netWorth = accounts.sumOf { it.balanceCents }

        val startNetWorth = netWorth - netCash
        val netWorthGrowthPct = when {
            startNetWorth > 0L -> ((netCash.toDouble() / startNetWorth.toDouble()) * 100.0).coerceIn(-999.0, 999.0)
            startNetWorth == 0L && netWorth > 0L -> 100.0
            startNetWorth == 0L && netWorth < 0L -> -100.0
            else -> 0.0
        }

        val incomeGrowthPct = when {
            prevInc > 0L -> (((income.toDouble() - prevInc.toDouble()) / prevInc.toDouble()) * 100.0).coerceIn(-999.0, 999.0)
            income > 0L -> 100.0
            else -> 0.0
        }

        val expenseGrowthPct = when {
            prevExp > 0L -> (((expense.toDouble() - prevExp.toDouble()) / prevExp.toDouble()) * 100.0).coerceIn(-999.0, 999.0)
            expense > 0L -> 100.0
            else -> 0.0
        }

        DashboardUiState(
            period = period,
            totalNetWorth = netWorth,
            totalIncome = income,
            totalExpense = expense,
            netCashFlow = netCash,
            netWorthGrowthPct = netWorthGrowthPct,
            incomeGrowthPct = incomeGrowthPct,
            expenseGrowthPct = expenseGrowthPct,
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

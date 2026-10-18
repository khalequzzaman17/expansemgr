package com.sysninja.expensetracker.data.model

enum class PeriodType {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    CUSTOM
}

data class CategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: String,
    val iconResName: String,
    val totalAmountCents: Long,
    val count: Int,
    val percentage: Double = 0.0
)

data class MonthlyFinanceSummary(
    val monthKey: String, // e.g. "2026-10"
    val displayLabel: String,
    val incomeCents: Long,
    val expenseCents: Long,
    val netCashFlowCents: Long
)

data class AccountBalanceItem(
    val accountId: Long,
    val accountName: String,
    val accountType: String,
    val colorHex: String,
    val iconName: String,
    val balanceCents: Long
)

data class BudgetProgressItem(
    val budgetId: Long,
    val categoryId: Long?,
    val categoryName: String,
    val limitCents: Long,
    val spentCents: Long,
    val remainingCents: Long,
    val usedPercentage: Int,
    val isOverBudget: Boolean,
    val isNearWarning: Boolean
)

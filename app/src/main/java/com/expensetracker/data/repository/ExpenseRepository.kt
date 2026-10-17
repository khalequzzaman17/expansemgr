package com.expensetracker.data.repository

import com.expensetracker.data.local.AppDatabase
import com.expensetracker.data.local.entity.*
import com.expensetracker.data.model.*
import com.expensetracker.domain.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Calendar

class ExpenseRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val accountDao = database.accountDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val recurringDao = database.recurringDao()
    private val savingsDao = database.savingsDao()
    private val debtDao = database.debtDao()

    // ==========================================
    // TRANSACTIONS
    // ==========================================

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.update(transaction.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteById(id)
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? = withContext(Dispatchers.IO) {
        transactionDao.getById(id)
    }

    fun getTransactionWithDetailsById(id: Long): Flow<TransactionWithDetails?> {
        return transactionDao.getWithDetailsById(id).flowOn(Dispatchers.IO)
    }

    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAllWithDetails().flowOn(Dispatchers.IO)
    }

    fun getRecentTransactionsWithDetails(limit: Int = 10): Flow<List<TransactionWithDetails>> {
        return transactionDao.getRecentWithDetails(limit).flowOn(Dispatchers.IO)
    }

    fun getTransactionsInPeriodWithDetails(startTime: Long, endTime: Long): Flow<List<TransactionWithDetails>> {
        return transactionDao.getInPeriodWithDetails(startTime, endTime).flowOn(Dispatchers.IO)
    }

    fun getFilteredTransactions(
        type: String?,
        categoryId: Long?,
        accountId: Long?,
        startTime: Long,
        endTime: Long,
        sortOrder: String = "NEWEST"
    ): Flow<List<TransactionWithDetails>> {
        return transactionDao.getFilteredWithDetails(type, categoryId, accountId, startTime, endTime, sortOrder).flowOn(Dispatchers.IO)
    }

    fun getTotalIncomeInPeriod(startTime: Long, endTime: Long): Flow<Long> {
        return transactionDao.getTotalIncomeInPeriod(startTime, endTime).flowOn(Dispatchers.IO)
    }

    fun getTotalExpenseInPeriod(startTime: Long, endTime: Long): Flow<Long> {
        return transactionDao.getTotalExpenseInPeriod(startTime, endTime).flowOn(Dispatchers.IO)
    }

    // ==========================================
    // ACCOUNTS & BALANCES
    // ==========================================

    fun getAllActiveAccounts(): Flow<List<AccountEntity>> = accountDao.getAllActive().flowOn(Dispatchers.IO)

    fun getAllAccounts(): Flow<List<AccountEntity>> = accountDao.getAll().flowOn(Dispatchers.IO)

    suspend fun getAccountById(id: Long): AccountEntity? = withContext(Dispatchers.IO) {
        accountDao.getById(id)
    }

    suspend fun insertAccount(account: AccountEntity): Long = withContext(Dispatchers.IO) {
        accountDao.insert(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.update(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.delete(account)
    }

    /**
     * Atomic fund transfer between accounts.
     * Records a single TRANSFER transaction, perfectly tracking both balances without double-counting income/expenses.
     */
    suspend fun transferFunds(
        fromAccountId: Long,
        toAccountId: Long,
        amountCents: Long,
        note: String?,
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val transferTx = TransactionEntity(
            type = "TRANSFER",
            title = "Transfer",
            amountCents = amountCents,
            accountId = fromAccountId,
            toAccountId = toAccountId,
            timestamp = timestamp,
            note = note,
            paymentMethod = "TRANSFER"
        )
        transactionDao.insert(transferTx)
    }

    /**
     * Calculates real-time calculated balances for all accounts based on initial balance,
     * income, expense, and atomic transfers.
     */
    fun getAccountBalances(): Flow<List<AccountBalanceItem>> {
        return combine(
            accountDao.getAllActive(),
            transactionDao.getAllWithDetails()
        ) { accounts, txs ->
            accounts.map { account ->
                var currentBalance = account.initialBalanceCents

                for (tx in txs) {
                    val entity = tx.transaction
                    when (entity.type) {
                        "INCOME" -> {
                            if (entity.accountId == account.id) {
                                currentBalance += entity.amountCents
                            }
                        }
                        "EXPENSE" -> {
                            if (entity.accountId == account.id) {
                                currentBalance -= entity.amountCents
                            }
                        }
                        "TRANSFER" -> {
                            if (entity.accountId == account.id) {
                                currentBalance -= entity.amountCents
                            }
                            if (entity.toAccountId == account.id) {
                                currentBalance += entity.amountCents
                            }
                        }
                    }
                }

                AccountBalanceItem(
                    accountId = account.id,
                    accountName = account.name,
                    accountType = account.type,
                    colorHex = account.colorHex,
                    iconName = account.iconName,
                    balanceCents = currentBalance
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getTotalNetWorth(): Flow<Long> {
        return getAccountBalances().combine(accountDao.getAllActive()) { balances, _ ->
            balances.sumOf { it.balanceCents }
        }.flowOn(Dispatchers.IO)
    }

    // ==========================================
    // CATEGORIES
    // ==========================================

    fun getAllActiveCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllActive().flowOn(Dispatchers.IO)

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> = categoryDao.getByType(type).flowOn(Dispatchers.IO)

    suspend fun getCategoryById(id: Long): CategoryEntity? = withContext(Dispatchers.IO) {
        categoryDao.getById(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.delete(category)
    }

    // ==========================================
    // BUDGETS
    // ==========================================

    fun getBudgetsForMonth(monthKey: String): Flow<List<BudgetProgressItem>> {
        val startOfMonth = DateUtils.getStartOfMonth()
        val endOfMonth = DateUtils.getEndOfMonth()

        return combine(
            budgetDao.getBudgetsForMonth(monthKey),
            transactionDao.getInPeriodWithDetails(startOfMonth, endOfMonth),
            categoryDao.getAllActive()
        ) { budgets, txs, categories ->
            val expenseTxs = txs.filter { it.transaction.type == "EXPENSE" }

            budgets.map { budget ->
                val category = categories.find { it.id == budget.categoryId }
                val categoryName = category?.name ?: "Total Monthly Budget"

                val spentCents = if (budget.categoryId == null) {
                    expenseTxs.sumOf { it.transaction.amountCents }
                } else {
                    expenseTxs.filter { it.transaction.categoryId == budget.categoryId }
                        .sumOf { it.transaction.amountCents }
                }

                val remainingCents = budget.limitAmountCents - spentCents
                val percentage = if (budget.limitAmountCents > 0) {
                    ((spentCents.toDouble() / budget.limitAmountCents.toDouble()) * 100.0).toInt().coerceIn(0, 1000)
                } else 0

                val isOverBudget = spentCents > budget.limitAmountCents
                val isNearWarning = !isOverBudget && percentage >= budget.warningThresholdPercent

                BudgetProgressItem(
                    budgetId = budget.id,
                    categoryId = budget.categoryId,
                    categoryName = categoryName,
                    limitCents = budget.limitAmountCents,
                    spentCents = spentCents,
                    remainingCents = remainingCents,
                    usedPercentage = percentage,
                    isOverBudget = isOverBudget,
                    isNearWarning = isNearWarning
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun insertBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        budgetDao.insert(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.update(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.delete(budget)
    }

    // ==========================================
    // RECURRING TRANSACTIONS
    // ==========================================

    fun getAllRecurring(): Flow<List<RecurringTransactionEntity>> = recurringDao.getAll().flowOn(Dispatchers.IO)

    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long = withContext(Dispatchers.IO) {
        recurringDao.insert(recurring)
    }

    suspend fun updateRecurring(recurring: RecurringTransactionEntity) = withContext(Dispatchers.IO) {
        recurringDao.update(recurring)
    }

    suspend fun deleteRecurring(recurring: RecurringTransactionEntity) = withContext(Dispatchers.IO) {
        recurringDao.delete(recurring)
    }

    /**
     * Executes due recurring transactions idempotently.
     */
    suspend fun executeDueRecurringTransactions(now: Long = System.currentTimeMillis()): Int = withContext(Dispatchers.IO) {
        val dueList = recurringDao.getDueTransactions(now)
        var executedCount = 0

        for (recurring in dueList) {
            // Check if end date passed
            if (recurring.endDate != null && recurring.nextDueDate > recurring.endDate) {
                recurringDao.update(recurring.copy(isPaused = true))
                continue
            }

            // Create actual transaction
            val newTx = TransactionEntity(
                type = recurring.type,
                title = recurring.title,
                amountCents = recurring.amountCents,
                categoryId = recurring.categoryId,
                accountId = recurring.accountId,
                toAccountId = recurring.toAccountId,
                timestamp = recurring.nextDueDate,
                note = recurring.note ?: "Auto-generated from recurring schedule",
                paymentMethod = recurring.paymentMethod
            )
            transactionDao.insert(newTx)

            // Compute next due date
            val nextDue = calculateNextDueDate(recurring.nextDueDate, recurring.frequency)
            recurringDao.update(
                recurring.copy(
                    lastExecutedDate = recurring.nextDueDate,
                    nextDueDate = nextDue
                )
            )
            executedCount++
        }
        executedCount
    }

    private fun calculateNextDueDate(currentDueDate: Long, frequency: String): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = currentDueDate
        when (frequency.uppercase()) {
            "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> cal.add(Calendar.MONTH, 1)
            "YEARLY" -> cal.add(Calendar.YEAR, 1)
            else -> cal.add(Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }

    // ==========================================
    // SAVINGS GOALS
    // ==========================================

    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>> = savingsDao.getAllGoals().flowOn(Dispatchers.IO)

    suspend fun getSavingsGoalById(id: Long): SavingsGoalEntity? = withContext(Dispatchers.IO) {
        savingsDao.getGoalById(id)
    }

    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long = withContext(Dispatchers.IO) {
        savingsDao.insertGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsDao.updateGoal(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsDao.deleteGoal(goal)
    }

    fun getSavingsLogsForGoal(goalId: Long): Flow<List<SavingsLogEntity>> = savingsDao.getLogsForGoal(goalId).flowOn(Dispatchers.IO)

    suspend fun recordSavingsMovement(
        goalId: Long,
        accountId: Long?,
        amountCents: Long,
        isDeposit: Boolean,
        note: String?
    ) = withContext(Dispatchers.IO) {
        val goal = savingsDao.getGoalById(goalId) ?: return@withContext
        val delta = if (isDeposit) amountCents else -amountCents
        val newCurrent = (goal.currentAmountCents + delta).coerceAtLeast(0L)
        val isCompleted = newCurrent >= goal.targetAmountCents

        savingsDao.insertLog(
            SavingsLogEntity(
                goalId = goalId,
                accountId = accountId,
                amountCents = delta,
                note = note
            )
        )
        savingsDao.updateGoal(
            goal.copy(
                currentAmountCents = newCurrent,
                isCompleted = isCompleted
            )
        )

        // If an account was chosen and funds moved, record movement transaction
        if (accountId != null && amountCents > 0L) {
            val txType = if (isDeposit) "EXPENSE" else "INCOME"
            val title = if (isDeposit) "Savings Deposit: ${goal.name}" else "Savings Withdrawal: ${goal.name}"
            transactionDao.insert(
                TransactionEntity(
                    type = txType,
                    title = title,
                    amountCents = amountCents,
                    accountId = accountId,
                    timestamp = System.currentTimeMillis(),
                    note = note ?: "Savings goal transaction"
                )
            )
        }
    }

    // ==========================================
    // DEBTS & REPAYMENTS
    // ==========================================

    fun getAllDebts(): Flow<List<DebtEntity>> = debtDao.getAllDebts().flowOn(Dispatchers.IO)

    suspend fun getDebtById(id: Long): DebtEntity? = withContext(Dispatchers.IO) {
        debtDao.getDebtById(id)
    }

    suspend fun insertDebt(debt: DebtEntity): Long = withContext(Dispatchers.IO) {
        debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) = withContext(Dispatchers.IO) {
        debtDao.deleteDebt(debt)
    }

    fun getRepaymentsForDebt(debtId: Long): Flow<List<DebtRepaymentEntity>> = debtDao.getRepaymentsForDebt(debtId).flowOn(Dispatchers.IO)

    suspend fun recordDebtRepayment(
        debtId: Long,
        accountId: Long?,
        amountCents: Long,
        note: String?
    ) = withContext(Dispatchers.IO) {
        val debt = debtDao.getDebtById(debtId) ?: return@withContext
        val newOutstanding = (debt.outstandingBalanceCents - amountCents).coerceAtLeast(0L)
        val newStatus = if (newOutstanding == 0L) "PAID" else debt.status

        debtDao.insertRepayment(
            DebtRepaymentEntity(
                debtId = debtId,
                accountId = accountId,
                amountCents = amountCents,
                notes = note
            )
        )
        debtDao.updateDebt(
            debt.copy(
                outstandingBalanceCents = newOutstanding,
                status = newStatus
            )
        )

        // Link with transaction if account specified
        if (accountId != null && amountCents > 0L) {
            // If PAYABLE (I owed): paying back is an EXPENSE
            // If RECEIVABLE (owed to me): receiving money is an INCOME
            val txType = if (debt.type == "PAYABLE") "EXPENSE" else "INCOME"
            val title = if (debt.type == "PAYABLE") "Debt Repayment: ${debt.personName}" else "Debt Collection: ${debt.personName}"
            transactionDao.insert(
                TransactionEntity(
                    type = txType,
                    title = title,
                    amountCents = amountCents,
                    accountId = accountId,
                    timestamp = System.currentTimeMillis(),
                    note = note ?: "Debt repayment record"
                )
            )
        }
    }

    // ==========================================
    // REPORTS & ANALYTICS
    // ==========================================

    fun getCategorySpendingInPeriod(startTime: Long, endTime: Long): Flow<List<CategorySpending>> {
        return combine(
            transactionDao.getInPeriodWithDetails(startTime, endTime),
            categoryDao.getAllActive()
        ) { txs, categories ->
            val expenseTxs = txs.filter { it.transaction.type == "EXPENSE" }
            val totalExpense = expenseTxs.sumOf { it.transaction.amountCents }

            val grouped = expenseTxs.groupBy { it.transaction.categoryId }
            grouped.map { (catId, items) ->
                val category = categories.find { it.id == catId }
                val catName = category?.name ?: "Uncategorized"
                val catColor = category?.colorHex ?: "#64748B"
                val icon = category?.iconResName ?: "ic_category"
                val sum = items.sumOf { it.transaction.amountCents }
                val percentage = if (totalExpense > 0L) {
                    (sum.toDouble() / totalExpense.toDouble()) * 100.0
                } else 0.0

                CategorySpending(
                    categoryId = catId ?: 0L,
                    categoryName = catName,
                    categoryColor = catColor,
                    iconResName = icon,
                    totalAmountCents = sum,
                    count = items.size,
                    percentage = percentage
                )
            }.sortedByDescending { it.totalAmountCents }
        }.flowOn(Dispatchers.IO)
    }

    fun getMonthlySummaries(monthCount: Int = 6): Flow<List<MonthlyFinanceSummary>> {
        return transactionDao.getAllWithDetails().combine(categoryDao.getAllActive()) { txs, _ ->
            val cal = Calendar.getInstance()
            val summaries = mutableListOf<MonthlyFinanceSummary>()

            for (i in (monthCount - 1) downTo 0) {
                val tempCal = Calendar.getInstance()
                tempCal.add(Calendar.MONTH, -i)
                val start = DateUtils.getStartOfMonth(tempCal.timeInMillis)
                val end = DateUtils.getEndOfMonth(tempCal.timeInMillis)
                val monthKey = DateUtils.toMonthKey(tempCal.timeInMillis)
                val display = DateUtils.formatMonthYear(tempCal.timeInMillis)

                val monthTxs = txs.filter { it.transaction.timestamp in start..end }
                val income = monthTxs.filter { it.transaction.type == "INCOME" }.sumOf { it.transaction.amountCents }
                val expense = monthTxs.filter { it.transaction.type == "EXPENSE" }.sumOf { it.transaction.amountCents }
                val net = income - expense

                summaries.add(
                    MonthlyFinanceSummary(
                        monthKey = monthKey,
                        displayLabel = display,
                        incomeCents = income,
                        expenseCents = expense,
                        netCashFlowCents = net
                    )
                )
            }
            summaries
        }.flowOn(Dispatchers.IO)
    }
}

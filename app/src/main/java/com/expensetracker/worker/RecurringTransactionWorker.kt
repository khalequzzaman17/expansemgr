package com.expensetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.expensetracker.data.local.AppDatabase
import com.expensetracker.data.repository.ExpenseRepository

class RecurringTransactionWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(context)
            val repository = ExpenseRepository(db)
            val executedCount = repository.executeDueRecurringTransactions()

            if (executedCount > 0) {
                ReminderNotificationHelper.showNotification(
                    context,
                    1001,
                    "Recurring Transactions Processed",
                    "$executedCount recurring transaction(s) have been automatically recorded."
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

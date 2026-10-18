package com.sysninja.expensetracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sysninja.expensetracker.data.local.AppDatabase
import com.sysninja.expensetracker.data.preferences.UserPreferencesManager
import com.sysninja.expensetracker.data.repository.ExpenseRepository
import com.sysninja.expensetracker.worker.RecurringTransactionWorker
import com.sysninja.expensetracker.worker.ReminderNotificationHelper
import java.util.concurrent.TimeUnit

class ExpenseTrackerApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: ExpenseRepository
        private set

    lateinit var preferencesManager: UserPreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = ExpenseRepository(database)
        preferencesManager = UserPreferencesManager(this)

        ReminderNotificationHelper.createNotificationChannel(this)
        scheduleRecurringWorker()
    }

    private fun scheduleRecurringWorker() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_transactions_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

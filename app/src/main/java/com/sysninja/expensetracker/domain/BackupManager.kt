package com.sysninja.expensetracker.domain

import com.sysninja.expensetracker.data.local.AppDatabase
import com.sysninja.expensetracker.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object BackupManager {

    private const val BACKUP_VERSION = 1

    suspend fun createBackup(database: AppDatabase, outputStream: OutputStream): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", BACKUP_VERSION)
            root.put("createdAt", System.currentTimeMillis())

            // Accounts
            val accountsArray = JSONArray()
            database.accountDao().getAllDirect().forEach { acc ->
                val obj = JSONObject().apply {
                    put("id", acc.id)
                    put("name", acc.name)
                    put("type", acc.type)
                    put("initialBalanceCents", acc.initialBalanceCents)
                    put("colorHex", acc.colorHex)
                    put("iconName", acc.iconName)
                    put("isArchived", acc.isArchived)
                    put("createdAt", acc.createdAt)
                }
                accountsArray.put(obj)
            }
            root.put("accounts", accountsArray)

            // Categories
            val categoriesArray = JSONArray()
            database.categoryDao().getAllDirect().forEach { cat ->
                val obj = JSONObject().apply {
                    put("id", cat.id)
                    put("name", cat.name)
                    put("type", cat.type)
                    put("iconResName", cat.iconResName)
                    put("colorHex", cat.colorHex)
                    put("isDefault", cat.isDefault)
                    put("isArchived", cat.isArchived)
                }
                categoriesArray.put(obj)
            }
            root.put("categories", categoriesArray)

            // Transactions
            val txArray = JSONArray()
            database.transactionDao().getAllDirect().forEach { tx ->
                val obj = JSONObject().apply {
                    put("id", tx.id)
                    put("type", tx.type)
                    put("title", tx.title)
                    put("amountCents", tx.amountCents)
                    put("categoryId", tx.categoryId ?: JSONObject.NULL)
                    put("accountId", tx.accountId)
                    put("toAccountId", tx.toAccountId ?: JSONObject.NULL)
                    put("timestamp", tx.timestamp)
                    put("note", tx.note ?: "")
                    put("tags", tx.tags ?: "")
                    put("paymentMethod", tx.paymentMethod)
                    put("createdAt", tx.createdAt)
                }
                txArray.put(obj)
            }
            root.put("transactions", txArray)

            // Budgets
            val budgetArray = JSONArray()
            database.budgetDao().getAllDirect().forEach { b ->
                val obj = JSONObject().apply {
                    put("id", b.id)
                    put("monthKey", b.monthKey)
                    put("categoryId", b.categoryId ?: JSONObject.NULL)
                    put("limitAmountCents", b.limitAmountCents)
                    put("warningThresholdPercent", b.warningThresholdPercent)
                }
                budgetArray.put(obj)
            }
            root.put("budgets", budgetArray)

            // Recurring
            val recurringArray = JSONArray()
            database.recurringDao().getAllDirect().forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("title", r.title)
                    put("type", r.type)
                    put("amountCents", r.amountCents)
                    put("categoryId", r.categoryId ?: JSONObject.NULL)
                    put("accountId", r.accountId)
                    put("toAccountId", r.toAccountId ?: JSONObject.NULL)
                    put("paymentMethod", r.paymentMethod)
                    put("frequency", r.frequency)
                    put("startDate", r.startDate)
                    put("endDate", r.endDate ?: JSONObject.NULL)
                    put("nextDueDate", r.nextDueDate)
                    put("lastExecutedDate", r.lastExecutedDate ?: JSONObject.NULL)
                    put("isPaused", r.isPaused)
                    put("note", r.note ?: "")
                }
                recurringArray.put(obj)
            }
            root.put("recurring", recurringArray)

            // Savings Goals & Logs
            val goalsArray = JSONArray()
            database.savingsDao().getAllGoalsDirect().forEach { g ->
                val obj = JSONObject().apply {
                    put("id", g.id)
                    put("name", g.name)
                    put("targetAmountCents", g.targetAmountCents)
                    put("currentAmountCents", g.currentAmountCents)
                    put("targetDate", g.targetDate ?: JSONObject.NULL)
                    put("colorHex", g.colorHex)
                    put("notes", g.notes ?: "")
                    put("isCompleted", g.isCompleted)
                }
                goalsArray.put(obj)
            }
            root.put("savingsGoals", goalsArray)

            val savingsLogsArray = JSONArray()
            database.savingsDao().getAllLogsDirect().forEach { l ->
                val obj = JSONObject().apply {
                    put("id", l.id)
                    put("goalId", l.goalId)
                    put("accountId", l.accountId ?: JSONObject.NULL)
                    put("amountCents", l.amountCents)
                    put("date", l.date)
                    put("note", l.note ?: "")
                }
                savingsLogsArray.put(obj)
            }
            root.put("savingsLogs", savingsLogsArray)

            // Debts & Repayments
            val debtsArray = JSONArray()
            database.debtDao().getAllDebtsDirect().forEach { d ->
                val obj = JSONObject().apply {
                    put("id", d.id)
                    put("type", d.type)
                    put("personName", d.personName)
                    put("originalAmountCents", d.originalAmountCents)
                    put("outstandingBalanceCents", d.outstandingBalanceCents)
                    put("dueDate", d.dueDate ?: JSONObject.NULL)
                    put("notes", d.notes ?: "")
                    put("status", d.status)
                }
                debtsArray.put(obj)
            }
            root.put("debts", debtsArray)

            val debtRepaymentsArray = JSONArray()
            database.debtDao().getAllRepaymentsDirect().forEach { rp ->
                val obj = JSONObject().apply {
                    put("id", rp.id)
                    put("debtId", rp.debtId)
                    put("accountId", rp.accountId ?: JSONObject.NULL)
                    put("amountCents", rp.amountCents)
                    put("date", rp.date)
                    put("notes", rp.notes ?: "")
                }
                debtRepaymentsArray.put(obj)
            }
            root.put("debtRepayments", debtRepaymentsArray)

            val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
            writer.write(root.toString(2))
            writer.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreBackup(database: AppDatabase, inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            val root = JSONObject(jsonString)

            val version = root.optInt("version", -1)
            if (version <= 0 || version > BACKUP_VERSION) {
                return@withContext false // Unsupported backup version
            }

            val accountsList = mutableListOf<AccountEntity>()
            val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
            for (i in 0 until accountsArray.length()) {
                val obj = accountsArray.getJSONObject(i)
                accountsList.add(
                    AccountEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        initialBalanceCents = obj.getLong("initialBalanceCents"),
                        colorHex = obj.optString("colorHex", "#2DD4BF"),
                        iconName = obj.optString("iconName", "ic_wallet"),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val categoriesList = mutableListOf<CategoryEntity>()
            val categoriesArray = root.optJSONArray("categories") ?: JSONArray()
            for (i in 0 until categoriesArray.length()) {
                val obj = categoriesArray.getJSONObject(i)
                categoriesList.add(
                    CategoryEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        iconResName = obj.optString("iconResName", "ic_category"),
                        colorHex = obj.optString("colorHex", "#2DD4BF"),
                        isDefault = obj.optBoolean("isDefault", false),
                        isArchived = obj.optBoolean("isArchived", false)
                    )
                )
            }

            val txList = mutableListOf<TransactionEntity>()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val catId = if (obj.isNull("categoryId")) null else obj.getLong("categoryId")
                val toAccId = if (obj.isNull("toAccountId")) null else obj.getLong("toAccountId")
                txList.add(
                    TransactionEntity(
                        id = obj.getLong("id"),
                        type = obj.getString("type"),
                        title = obj.getString("title"),
                        amountCents = obj.getLong("amountCents"),
                        categoryId = catId,
                        accountId = obj.getLong("accountId"),
                        toAccountId = toAccId,
                        timestamp = obj.getLong("timestamp"),
                        note = obj.optString("note", ""),
                        tags = obj.optString("tags", ""),
                        paymentMethod = obj.optString("paymentMethod", "CASH"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val budgetList = mutableListOf<BudgetEntity>()
            val budgetArray = root.optJSONArray("budgets") ?: JSONArray()
            for (i in 0 until budgetArray.length()) {
                val obj = budgetArray.getJSONObject(i)
                val catId = if (obj.isNull("categoryId")) null else obj.getLong("categoryId")
                budgetList.add(
                    BudgetEntity(
                        id = obj.getLong("id"),
                        monthKey = obj.getString("monthKey"),
                        categoryId = catId,
                        limitAmountCents = obj.getLong("limitAmountCents"),
                        warningThresholdPercent = obj.optInt("warningThresholdPercent", 80)
                    )
                )
            }

            val recurringList = mutableListOf<RecurringTransactionEntity>()
            val recurringArray = root.optJSONArray("recurring") ?: JSONArray()
            for (i in 0 until recurringArray.length()) {
                val obj = recurringArray.getJSONObject(i)
                recurringList.add(
                    RecurringTransactionEntity(
                        id = obj.getLong("id"),
                        title = obj.getString("title"),
                        type = obj.getString("type"),
                        amountCents = obj.getLong("amountCents"),
                        categoryId = if (obj.isNull("categoryId")) null else obj.getLong("categoryId"),
                        accountId = obj.getLong("accountId"),
                        toAccountId = if (obj.isNull("toAccountId")) null else obj.getLong("toAccountId"),
                        paymentMethod = obj.optString("paymentMethod", "CASH"),
                        frequency = obj.getString("frequency"),
                        startDate = obj.getLong("startDate"),
                        endDate = if (obj.isNull("endDate")) null else obj.getLong("endDate"),
                        nextDueDate = obj.getLong("nextDueDate"),
                        lastExecutedDate = if (obj.isNull("lastExecutedDate")) null else obj.getLong("lastExecutedDate"),
                        isPaused = obj.optBoolean("isPaused", false),
                        note = obj.optString("note", "")
                    )
                )
            }

            val goalsList = mutableListOf<SavingsGoalEntity>()
            val goalsArray = root.optJSONArray("savingsGoals") ?: JSONArray()
            for (i in 0 until goalsArray.length()) {
                val obj = goalsArray.getJSONObject(i)
                goalsList.add(
                    SavingsGoalEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        targetAmountCents = obj.getLong("targetAmountCents"),
                        currentAmountCents = obj.getLong("currentAmountCents"),
                        targetDate = if (obj.isNull("targetDate")) null else obj.getLong("targetDate"),
                        colorHex = obj.optString("colorHex", "#A855F7"),
                        notes = obj.optString("notes", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }

            val savingsLogsList = mutableListOf<SavingsLogEntity>()
            val savingsLogsArray = root.optJSONArray("savingsLogs") ?: JSONArray()
            for (i in 0 until savingsLogsArray.length()) {
                val obj = savingsLogsArray.getJSONObject(i)
                savingsLogsList.add(
                    SavingsLogEntity(
                        id = obj.getLong("id"),
                        goalId = obj.getLong("goalId"),
                        accountId = if (obj.isNull("accountId")) null else obj.getLong("accountId"),
                        amountCents = obj.getLong("amountCents"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        note = obj.optString("note", "")
                    )
                )
            }

            val debtsList = mutableListOf<DebtEntity>()
            val debtsArray = root.optJSONArray("debts") ?: JSONArray()
            for (i in 0 until debtsArray.length()) {
                val obj = debtsArray.getJSONObject(i)
                debtsList.add(
                    DebtEntity(
                        id = obj.getLong("id"),
                        type = obj.getString("type"),
                        personName = obj.getString("personName"),
                        originalAmountCents = obj.getLong("originalAmountCents"),
                        outstandingBalanceCents = obj.getLong("outstandingBalanceCents"),
                        dueDate = if (obj.isNull("dueDate")) null else obj.getLong("dueDate"),
                        notes = obj.optString("notes", ""),
                        status = obj.optString("status", "PENDING")
                    )
                )
            }

            val debtRepaymentsList = mutableListOf<DebtRepaymentEntity>()
            val debtRepaymentsArray = root.optJSONArray("debtRepayments") ?: JSONArray()
            for (i in 0 until debtRepaymentsArray.length()) {
                val obj = debtRepaymentsArray.getJSONObject(i)
                debtRepaymentsList.add(
                    DebtRepaymentEntity(
                        id = obj.getLong("id"),
                        debtId = obj.getLong("debtId"),
                        accountId = if (obj.isNull("accountId")) null else obj.getLong("accountId"),
                        amountCents = obj.getLong("amountCents"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }

            // Restore into database atomically
            database.runInTransaction {
                // Clear existing records
                kotlinx.coroutines.runBlocking {
                    database.transactionDao().deleteAll()
                    database.budgetDao().deleteAll()
                    database.recurringDao().deleteAll()
                    database.savingsDao().deleteAllLogs()
                    database.savingsDao().deleteAllGoals()
                    database.debtDao().deleteAllRepayments()
                    database.debtDao().deleteAllDebts()
                    database.categoryDao().deleteAll()
                    database.accountDao().deleteAll()

                    // Insert restored records
                    database.accountDao().insertAll(accountsList)
                    database.categoryDao().insertAll(categoriesList)
                    database.transactionDao().insertAll(txList)
                    database.budgetDao().insertAll(budgetList)
                    database.recurringDao().insertAll(recurringList)
                    database.savingsDao().insertAllGoals(goalsList)
                    database.savingsDao().insertAllLogs(savingsLogsList)
                    database.debtDao().insertAllDebts(debtsList)
                    database.debtDao().insertAllRepayments(debtRepaymentsList)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

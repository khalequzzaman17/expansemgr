package com.sysninja.expensetracker.data.local.dao

import androidx.room.*
import com.sysninja.expensetracker.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recurringList: List<RecurringTransactionEntity>)

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurring: RecurringTransactionEntity)

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Query("SELECT * FROM recurring_transactions ORDER BY isPaused ASC, nextDueDate ASC")
    fun getAll(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE isPaused = 0 AND nextDueDate <= :timestamp")
    suspend fun getDueTransactions(timestamp: Long): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions")
    suspend fun getAllDirect(): List<RecurringTransactionEntity>

    @Query("DELETE FROM recurring_transactions")
    suspend fun deleteAll()
}

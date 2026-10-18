package com.sysninja.expensetracker.data.local.dao

import androidx.room.*
import com.sysninja.expensetracker.data.local.entity.TransactionEntity
import com.sysninja.expensetracker.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getWithDetailsById(id: Long): Flow<TransactionWithDetails?>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC")
    fun getAllWithDetails(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC, id DESC")
    fun getInPeriodWithDetails(startTime: Long, endTime: Long): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun getRecentWithDetails(limit: Int): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM transactions 
        WHERE (:type IS NULL OR type = :type)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:accountId IS NULL OR accountId = :accountId OR toAccountId = :accountId)
          AND timestamp BETWEEN :startTime AND :endTime
        ORDER BY 
          CASE WHEN :sortOrder = 'NEWEST' THEN timestamp END DESC,
          CASE WHEN :sortOrder = 'OLDEST' THEN timestamp END ASC,
          CASE WHEN :sortOrder = 'HIGHEST' THEN amountCents END DESC,
          CASE WHEN :sortOrder = 'LOWEST' THEN amountCents END ASC,
          id DESC
    """)
    fun getFilteredWithDetails(
        type: String?,
        categoryId: Long?,
        accountId: Long?,
        startTime: Long,
        endTime: Long,
        sortOrder: String = "NEWEST"
    ): Flow<List<TransactionWithDetails>>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE type = 'INCOME' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalIncomeInPeriod(startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountCents), 0) FROM transactions WHERE type = 'EXPENSE' AND timestamp BETWEEN :startTime AND :endTime")
    fun getTotalExpenseInPeriod(startTime: Long, endTime: Long): Flow<Long>

    @Query("SELECT * FROM transactions")
    suspend fun getAllDirect(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

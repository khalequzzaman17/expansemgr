package com.sysninja.expensetracker.data.local.dao

import androidx.room.*
import com.sysninja.expensetracker.data.local.entity.DebtEntity
import com.sysninja.expensetracker.data.local.entity.DebtRepaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDebts(debts: List<DebtEntity>)

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts ORDER BY status ASC, createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepayment(repayment: DebtRepaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRepayments(repayments: List<DebtRepaymentEntity>)

    @Query("SELECT * FROM debt_repayments WHERE debtId = :debtId ORDER BY date DESC")
    fun getRepaymentsForDebt(debtId: Long): Flow<List<DebtRepaymentEntity>>

    @Query("SELECT * FROM debts")
    suspend fun getAllDebtsDirect(): List<DebtEntity>

    @Query("SELECT * FROM debt_repayments")
    suspend fun getAllRepaymentsDirect(): List<DebtRepaymentEntity>

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Query("DELETE FROM debt_repayments")
    suspend fun deleteAllRepayments()
}

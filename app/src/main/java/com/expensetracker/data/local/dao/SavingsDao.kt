package com.expensetracker.data.local.dao

import androidx.room.*
import com.expensetracker.data.local.entity.SavingsGoalEntity
import com.expensetracker.data.local.entity.SavingsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoalEntity?

    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SavingsLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<SavingsLogEntity>)

    @Query("SELECT * FROM savings_logs WHERE goalId = :goalId ORDER BY date DESC")
    fun getLogsForGoal(goalId: Long): Flow<List<SavingsLogEntity>>

    @Query("SELECT * FROM savings_goals")
    suspend fun getAllGoalsDirect(): List<SavingsGoalEntity>

    @Query("SELECT * FROM savings_logs")
    suspend fun getAllLogsDirect(): List<SavingsLogEntity>

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAllGoals()

    @Query("DELETE FROM savings_logs")
    suspend fun deleteAllLogs()
}

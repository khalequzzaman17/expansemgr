package com.sysninja.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // PAYABLE (I owe), RECEIVABLE (Owed to me)
    val personName: String,
    val originalAmountCents: Long,
    val outstandingBalanceCents: Long,
    val dueDate: Long? = null,
    val notes: String? = null,
    val status: String = "PENDING", // PENDING, PAID, OVERDUE
    val createdAt: Long = System.currentTimeMillis()
)

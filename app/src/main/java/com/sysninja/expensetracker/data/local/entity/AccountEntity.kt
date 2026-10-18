package com.sysninja.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // CASH, BANK, BKASH, NAGAD, CARD, OTHER
    val initialBalanceCents: Long = 0L,
    val colorHex: String = "#2DD4BF",
    val iconName: String = "ic_wallet",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

package com.sysninja.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // EXPENSE, INCOME
    val iconResName: String = "ic_category",
    val colorHex: String = "#2DD4BF",
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)

package com.sysninja.expensetracker.data.local

import com.sysninja.expensetracker.data.local.entity.AccountEntity
import com.sysninja.expensetracker.data.local.entity.CategoryEntity

object PrepopulateData {

    fun getDefaultCategories(): List<CategoryEntity> = listOf(
        // Expense Categories
        CategoryEntity(name = "Food and Groceries", type = "EXPENSE", iconResName = "ic_category", colorHex = "#10B981", isDefault = true),
        CategoryEntity(name = "Transport", type = "EXPENSE", iconResName = "ic_transfer", colorHex = "#38BDF8", isDefault = true),
        CategoryEntity(name = "Shopping", type = "EXPENSE", iconResName = "ic_category", colorHex = "#F43F5E", isDefault = true),
        CategoryEntity(name = "Bills", type = "EXPENSE", iconResName = "ic_receipt", colorHex = "#F59E0B", isDefault = true),
        CategoryEntity(name = "Rent", type = "EXPENSE", iconResName = "ic_home", colorHex = "#8B5CF6", isDefault = true),
        CategoryEntity(name = "Healthcare", type = "EXPENSE", iconResName = "ic_category", colorHex = "#EC4899", isDefault = true),
        CategoryEntity(name = "Education", type = "EXPENSE", iconResName = "ic_category", colorHex = "#06B6D4", isDefault = true),
        CategoryEntity(name = "Entertainment", type = "EXPENSE", iconResName = "ic_category", colorHex = "#84CC16", isDefault = true),
        CategoryEntity(name = "Travel", type = "EXPENSE", iconResName = "ic_category", colorHex = "#14B8A6", isDefault = true),
        CategoryEntity(name = "Personal Care", type = "EXPENSE", iconResName = "ic_category", colorHex = "#A855F7", isDefault = true),
        CategoryEntity(name = "Family", type = "EXPENSE", iconResName = "ic_category", colorHex = "#EAB308", isDefault = true),
        CategoryEntity(name = "Other", type = "EXPENSE", iconResName = "ic_category", colorHex = "#64748B", isDefault = true),

        // Income Categories
        CategoryEntity(name = "Salary", type = "INCOME", iconResName = "ic_wallet", colorHex = "#10B981", isDefault = true),
        CategoryEntity(name = "Freelance", type = "INCOME", iconResName = "ic_income", colorHex = "#06B6D4", isDefault = true),
        CategoryEntity(name = "Business", type = "INCOME", iconResName = "ic_income", colorHex = "#3B82F6", isDefault = true),
        CategoryEntity(name = "Investment Returns", type = "INCOME", iconResName = "ic_analytics", colorHex = "#8B5CF6", isDefault = true),
        CategoryEntity(name = "Gifts", type = "INCOME", iconResName = "ic_category", colorHex = "#EC4899", isDefault = true),
        CategoryEntity(name = "Other Income", type = "INCOME", iconResName = "ic_income", colorHex = "#64748B", isDefault = true)
    )

    fun getDefaultAccounts(): List<AccountEntity> = listOf(
        AccountEntity(name = "Cash", type = "CASH", initialBalanceCents = 0L, colorHex = "#10B981", iconName = "ic_wallet"),
        AccountEntity(name = "Bank", type = "BANK", initialBalanceCents = 0L, colorHex = "#3B82F6", iconName = "ic_wallet"),
        AccountEntity(name = "bKash", type = "BKASH", initialBalanceCents = 0L, colorHex = "#EC4899", iconName = "ic_wallet"),
        AccountEntity(name = "Nagad", type = "NAGAD", initialBalanceCents = 0L, colorHex = "#F97316", iconName = "ic_wallet")
    )
}

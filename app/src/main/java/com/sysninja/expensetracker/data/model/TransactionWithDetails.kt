package com.sysninja.expensetracker.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.sysninja.expensetracker.data.local.entity.AccountEntity
import com.sysninja.expensetracker.data.local.entity.CategoryEntity
import com.sysninja.expensetracker.data.local.entity.TransactionEntity

data class TransactionWithDetails(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,

    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity?,

    @Relation(
        parentColumn = "toAccountId",
        entityColumn = "id"
    )
    val toAccount: AccountEntity?
)

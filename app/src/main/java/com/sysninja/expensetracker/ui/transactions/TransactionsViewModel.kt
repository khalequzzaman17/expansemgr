package com.sysninja.expensetracker.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.data.model.TransactionWithDetails
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExpenseTrackerApp
    private val repository = app.repository
    val preferences = app.preferencesManager

    val filterType = MutableStateFlow<String?>(null) // null = ALL, "EXPENSE", "INCOME", "TRANSFER"
    val filterCategoryId = MutableStateFlow<Long?>(null)
    val filterAccountId = MutableStateFlow<Long?>(null)
    val sortOrder = MutableStateFlow("NEWEST") // "NEWEST", "OLDEST", "HIGHEST", "LOWEST"
    val searchQuery = MutableStateFlow("")

    val startDate = MutableStateFlow(0L)
    val endDate = MutableStateFlow(Long.MAX_VALUE)

    val transactions: StateFlow<List<TransactionWithDetails>> = combine(
        filterType,
        filterCategoryId,
        filterAccountId,
        combine(sortOrder, searchQuery) { sort, query -> sort to query },
        combine(startDate, endDate) { start, end -> start to end }
    ) { type, catId, accId, (sort, query), (start, end) ->
        FilterParams(type, catId, accId, sort, query, start, end)
    }.flatMapLatest { params ->
        repository.getFilteredTransactions(
            type = params.type,
            categoryId = params.categoryId,
            accountId = params.accountId,
            startTime = params.start,
            endTime = params.end,
            sortOrder = params.sort
        ).map { list ->
            if (params.query.isBlank()) {
                list
            } else {
                val q = params.query.trim().lowercase()
                list.filter { item ->
                    item.transaction.title.lowercase().contains(q) ||
                    (item.transaction.note?.lowercase()?.contains(q) == true) ||
                    (item.transaction.tags?.lowercase()?.contains(q) == true) ||
                    (item.category?.name?.lowercase()?.contains(q) == true)
                }
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun deleteTransaction(item: TransactionWithDetails) {
        viewModelScope.launch {
            repository.deleteTransaction(item.transaction)
        }
    }

    private data class FilterParams(
        val type: String?,
        val categoryId: Long?,
        val accountId: Long?,
        val sort: String,
        val query: String,
        val start: Long,
        val end: Long
    )
}

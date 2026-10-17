package com.expensetracker.ui.addtransaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.R
import com.expensetracker.data.local.entity.AccountEntity
import com.expensetracker.data.local.entity.CategoryEntity
import com.expensetracker.data.local.entity.TransactionEntity
import com.expensetracker.databinding.ActivityAddEditTransactionBinding
import com.expensetracker.domain.DateUtils
import com.expensetracker.domain.MoneyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEditTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTransactionBinding
    private var transactionId: Long = 0L
    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var currentType: String = "EXPENSE"

    private var accounts: List<AccountEntity> = emptyList()
    private var categories: List<CategoryEntity> = emptyList()

    private val paymentMethods = listOf("CASH", "BANK", "CARD", "BKASH", "NAGAD", "OTHER")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        transactionId = intent.getLongExtra("EXTRA_TRANSACTION_ID", 0L)
        val initialType = intent.getStringExtra("EXTRA_TYPE") ?: "EXPENSE"

        setupTypeSelector(initialType)
        setupPaymentMethodSpinner()
        setupDatePicker()
        setupSaveButton()

        loadAccountsAndCategories {
            if (transactionId > 0L) {
                binding.toolbar.title = getString(R.string.title_edit_transaction)
                loadExistingTransaction(transactionId)
            } else {
                binding.toolbar.title = getString(R.string.title_add_transaction)
                updateDateTimeButton()
            }
        }
    }

    private fun setupTypeSelector(initialType: String) {
        currentType = initialType
        when (initialType) {
            "EXPENSE" -> binding.chipExpense.isChecked = true
            "INCOME" -> binding.chipIncome.isChecked = true
            "TRANSFER" -> binding.chipTransfer.isChecked = true
        }
        updateTypeViews(currentType)

        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            currentType = when {
                checkedIds.contains(R.id.chipExpense) -> "EXPENSE"
                checkedIds.contains(R.id.chipIncome) -> "INCOME"
                checkedIds.contains(R.id.chipTransfer) -> "TRANSFER"
                else -> "EXPENSE"
            }
            updateTypeViews(currentType)
            loadCategoriesForType(currentType)
        }
    }

    private fun updateTypeViews(type: String) {
        if (type == "TRANSFER") {
            binding.layoutCategory.visibility = View.GONE
            binding.layoutToAccount.visibility = View.VISIBLE
        } else {
            binding.layoutCategory.visibility = View.VISIBLE
            binding.layoutToAccount.visibility = View.GONE
        }
    }

    private fun setupPaymentMethodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paymentMethods)
        binding.spinnerPaymentMethod.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedTimestamp }
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            cal.set(Calendar.MINUTE, minute)
                            selectedTimestamp = cal.timeInMillis
                            updateDateTimeButton()
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateTimeButton() {
        binding.btnPickDate.text = DateUtils.formatDateTime(selectedTimestamp)
    }

    private fun loadAccountsAndCategories(onComplete: () -> Unit) {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            binding.tvCurrencySymbol.text = currency

            accounts = app.repository.getAllActiveAccounts().first()
            val accountNames = accounts.map { it.name }

            val accAdapter = ArrayAdapter(
                this@AddEditTransactionActivity,
                android.R.layout.simple_spinner_dropdown_item,
                accountNames
            )
            binding.spinnerAccount.adapter = accAdapter
            binding.spinnerToAccount.adapter = accAdapter

            loadCategoriesForType(currentType)
            onComplete()
        }
    }

    private fun loadCategoriesForType(type: String) {
        if (type == "TRANSFER") return
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            categories = app.repository.getCategoriesByType(type).first()
            val catNames = categories.map { it.name }
            val catAdapter = ArrayAdapter(
                this@AddEditTransactionActivity,
                android.R.layout.simple_spinner_dropdown_item,
                catNames
            )
            binding.spinnerCategory.adapter = catAdapter
        }
    }

    private fun loadExistingTransaction(id: Long) {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val tx = app.repository.getTransactionById(id) ?: return@launch
            selectedTimestamp = tx.timestamp
            updateDateTimeButton()

            binding.etTitle.setText(tx.title)
            val amountFormatted = MoneyUtils.toBigDecimal(tx.amountCents).toPlainString()
            binding.etAmount.setText(amountFormatted)
            binding.etNote.setText(tx.note ?: "")
            binding.etTags.setText(tx.tags ?: "")

            currentType = tx.type
            when (tx.type) {
                "EXPENSE" -> binding.chipExpense.isChecked = true
                "INCOME" -> binding.chipIncome.isChecked = true
                "TRANSFER" -> binding.chipTransfer.isChecked = true
            }
            updateTypeViews(currentType)

            // Select account
            val accIndex = accounts.indexOfFirst { it.id == tx.accountId }
            if (accIndex >= 0) binding.spinnerAccount.setSelection(accIndex)

            if (tx.toAccountId != null) {
                val toAccIndex = accounts.indexOfFirst { it.id == tx.toAccountId }
                if (toAccIndex >= 0) binding.spinnerToAccount.setSelection(toAccIndex)
            }

            // Select category
            if (tx.categoryId != null) {
                val catIndex = categories.indexOfFirst { it.id == tx.categoryId }
                if (catIndex >= 0) binding.spinnerCategory.setSelection(catIndex)
            }

            // Select payment method
            val pmIndex = paymentMethods.indexOf(tx.paymentMethod)
            if (pmIndex >= 0) binding.spinnerPaymentMethod.setSelection(pmIndex)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString().trim()
        val amountCents = MoneyUtils.parseToCents(amountStr)
        if (amountCents <= 0L) {
            showError(getString(R.string.error_invalid_amount))
            return
        }

        var title = binding.etTitle.text.toString().trim()
        if (title.isBlank()) {
            title = if (currentType == "TRANSFER") "Transfer" else currentType.lowercase().replaceFirstChar { it.uppercase() }
        }

        if (accounts.isEmpty()) {
            showError("No accounts found. Please wait or create an account.")
            return
        }

        val fromAccount = accounts.getOrNull(binding.spinnerAccount.selectedItemPosition) ?: accounts[0]
        var toAccount: AccountEntity? = null
        var categoryId: Long? = null

        if (currentType == "TRANSFER") {
            toAccount = accounts.getOrNull(binding.spinnerToAccount.selectedItemPosition)
            if (toAccount == null || toAccount.id == fromAccount.id) {
                showError(getString(R.string.error_same_account))
                return
            }
        } else {
            val selectedCat = categories.getOrNull(binding.spinnerCategory.selectedItemPosition)
            categoryId = selectedCat?.id
        }

        val paymentMethod = paymentMethods.getOrNull(binding.spinnerPaymentMethod.selectedItemPosition) ?: "CASH"
        val note = binding.etNote.text.toString().trim().ifBlank { null }
        val tags = binding.etTags.text.toString().trim().ifBlank { null }

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            if (transactionId > 0L) {
                val updated = TransactionEntity(
                    id = transactionId,
                    type = currentType,
                    title = title,
                    amountCents = amountCents,
                    categoryId = categoryId,
                    accountId = fromAccount.id,
                    toAccountId = toAccount?.id,
                    timestamp = selectedTimestamp,
                    note = note,
                    tags = tags,
                    paymentMethod = paymentMethod
                )
                app.repository.updateTransaction(updated)
            } else {
                val newTx = TransactionEntity(
                    type = currentType,
                    title = title,
                    amountCents = amountCents,
                    categoryId = categoryId,
                    accountId = fromAccount.id,
                    toAccountId = toAccount?.id,
                    timestamp = selectedTimestamp,
                    note = note,
                    tags = tags,
                    paymentMethod = paymentMethod
                )
                app.repository.insertTransaction(newTx)
            }

            Toast.makeText(this@AddEditTransactionActivity, R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showError(msg: String) {
        binding.tvErrorMessage.text = msg
        binding.tvErrorMessage.visibility = View.VISIBLE
    }
}

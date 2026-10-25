package com.sysninja.expensetracker.ui.debts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.DebtEntity
import com.sysninja.expensetracker.databinding.ActivityDebtsBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DebtsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebtsBinding
    private lateinit var adapter: DebtAdapter
    private var allDebts = listOf<DebtEntity>()
    private var currentFilter = FilterType.ALL
    private var currencySymbol: String = "৳"

    enum class FilterType {
        ALL, RECEIVABLE, PAYABLE, SETTLED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebtsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = DebtAdapter(
            currencySymbol = currencySymbol,
            onDebtClick = { debt ->
                // show details or settlement
                showSettlementDialog(debt)
            },
            onRecordSettlement = { debt ->
                showSettlementDialog(debt)
            },
            onDeleteDebt = { debt ->
                confirmDeleteDebt(debt)
            }
        )
        binding.rvDebts.layoutManager = LinearLayoutManager(this)
        binding.rvDebts.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddNewDebt.setOnClickListener {
            startActivity(Intent(this, AddEditDebtActivity::class.java))
        }

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when {
                checkedIds.contains(R.id.chipReceivable) -> FilterType.RECEIVABLE
                checkedIds.contains(R.id.chipPayable) -> FilterType.PAYABLE
                checkedIds.contains(R.id.chipSettled) -> FilterType.SETTLED
                else -> FilterType.ALL
            }
            applyFilter()
        }
    }

    private fun observeData() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            currencySymbol = app.preferencesManager.currencySymbolFlow.first()
            app.repository.getAllDebts().collect { debts ->
                allDebts = debts
                updateOverview(debts)
                applyFilter()
            }
        }
    }

    private fun updateOverview(debts: List<DebtEntity>) {
        var totalReceivable = 0L
        var totalPayable = 0L
        var countReceivable = 0
        var countPayable = 0

        debts.forEach { d ->
            val isPaid = d.status.equals("PAID", ignoreCase = true) || d.outstandingBalanceCents <= 0L
            if (!isPaid) {
                if (d.type.equals("RECEIVABLE", ignoreCase = true)) {
                    totalReceivable += d.outstandingBalanceCents
                    countReceivable++
                } else {
                    totalPayable += d.outstandingBalanceCents
                    countPayable++
                }
            }
        }

        val net = totalReceivable - totalPayable
        val netPrefix = if (net >= 0) "+" else "-"
        val netFormatted = MoneyUtils.formatCents(Math.abs(net), currencySymbol)
        binding.tvNetDebtAmount.text = "$netPrefix$netFormatted"

        binding.tvReceivableTotal.text = "+${MoneyUtils.formatCents(totalReceivable, currencySymbol)}"
        binding.tvReceivableCount.text = getString(R.string.persons_count, countReceivable)

        binding.tvPayableTotal.text = "-${MoneyUtils.formatCents(totalPayable, currencySymbol)}"
        binding.tvPayableCount.text = getString(R.string.persons_count, countPayable)
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            FilterType.ALL -> allDebts
            FilterType.RECEIVABLE -> allDebts.filter {
                it.type.equals("RECEIVABLE", ignoreCase = true) &&
                        !it.status.equals("PAID", ignoreCase = true) &&
                        it.outstandingBalanceCents > 0L
            }
            FilterType.PAYABLE -> allDebts.filter {
                it.type.equals("PAYABLE", ignoreCase = true) &&
                        !it.status.equals("PAID", ignoreCase = true) &&
                        it.outstandingBalanceCents > 0L
            }
            FilterType.SETTLED -> allDebts.filter {
                it.status.equals("PAID", ignoreCase = true) || it.outstandingBalanceCents <= 0L
            }
        }

        adapter.submitList(filtered, currencySymbol)

        if (filtered.isEmpty()) {
            binding.rvDebts.visibility = View.GONE
            binding.llEmptyDebts.visibility = View.VISIBLE
        } else {
            binding.rvDebts.visibility = View.VISIBLE
            binding.llEmptyDebts.visibility = View.GONE
        }
    }

    private fun showSettlementDialog(debt: DebtEntity) {
        val isReceivable = debt.type.equals("RECEIVABLE", ignoreCase = true)
        val titleText = if (isReceivable) getString(R.string.repayment_dialog_title, debt.personName) else getString(R.string.pay_debt)

        val input = EditText(this).apply {
            hint = getString(R.string.dialog_enter_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(MoneyUtils.toBigDecimal(debt.outstandingBalanceCents).toPlainString())
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_muted))
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle(titleText)
            .setMessage("${debt.personName}\n${getString(R.string.dialog_remaining_balance, MoneyUtils.formatCents(debt.outstandingBalanceCents, currencySymbol))}")
            .setView(input)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val amtStr = input.text.toString().trim()
                val parsed = MoneyUtils.parseToCents(amtStr)
                if (parsed != null && parsed > 0) {
                    val app = application as ExpenseTrackerApp
                    lifecycleScope.launch {
                        app.repository.recordDebtRepayment(debt.id, null, parsed, "Debt settlement")
                        Toast.makeText(this@DebtsActivity, getString(R.string.repayment_recorded), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun confirmDeleteDebt(debt: DebtEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_debt_title))
            .setMessage(getString(R.string.confirm_delete_debt_msg, debt.personName))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    app.repository.deleteDebt(debt)
                    Toast.makeText(this@DebtsActivity, getString(R.string.tx_deleted_success), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}

package com.sysninja.expensetracker.ui.recurring

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.RecurringTransactionEntity
import com.sysninja.expensetracker.databinding.ActivityRecurringBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RecurringActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecurringBinding
    private lateinit var adapter: RecurringAdapter
    private var allItems = listOf<RecurringTransactionEntity>()
    private var currentFilter = "ALL"
    private var currencySymbol: String = "৳"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecurringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = RecurringAdapter(
            currencySymbol = currencySymbol,
            onRecurringClick = { item ->
                val intent = Intent(this, AddEditRecurringActivity::class.java).apply {
                    putExtra("recurring_id", item.id)
                }
                startActivity(intent)
            },
            onTogglePause = { item ->
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val updated = item.copy(isPaused = !item.isPaused)
                    app.repository.updateRecurring(updated)
                    val statusMsg = if (updated.isPaused) getString(R.string.recurring_paused) else getString(R.string.recurring_resumed)
                    Toast.makeText(this@RecurringActivity, statusMsg, Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteRecurring = { item ->
                confirmDelete(item)
            }
        )
        binding.rvRecurring.layoutManager = LinearLayoutManager(this)
        binding.rvRecurring.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddNewRecurring.setOnClickListener {
            startActivity(Intent(this, AddEditRecurringActivity::class.java))
        }

        binding.chipGroupRecurring.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when {
                checkedIds.contains(R.id.chipRecurringMonthly) -> "MONTHLY"
                checkedIds.contains(R.id.chipRecurringWeekly) -> "WEEKLY"
                checkedIds.contains(R.id.chipRecurringYearly) -> "YEARLY"
                else -> "ALL"
            }
            applyFilter()
        }
    }

    private fun observeData() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            currencySymbol = app.preferencesManager.currencySymbolFlow.first()
            app.repository.getAllRecurring().collect { items ->
                allItems = items
                updateOverview(items)
                applyFilter()
            }
        }
    }

    private fun updateOverview(items: List<RecurringTransactionEntity>) {
        var monthlyTotalCents = 0L
        var activeCount = 0
        var upcoming7DaysCount = 0
        var upcoming7DaysAmount = 0L

        val now = System.currentTimeMillis()
        val sevenDaysMs = TimeUnit.DAYS.toMillis(7)

        items.forEach { item ->
            if (!item.isPaused) {
                activeCount++
                val monthlyEquivalent = when (item.frequency.uppercase()) {
                    "DAILY" -> item.amountCents * 30
                    "WEEKLY" -> item.amountCents * 4
                    "YEARLY" -> item.amountCents / 12
                    else -> item.amountCents
                }
                monthlyTotalCents += monthlyEquivalent

                val diff = item.nextDueDate - now
                if (diff in 0..sevenDaysMs) {
                    upcoming7DaysCount++
                    upcoming7DaysAmount += item.amountCents
                }
            }
        }

        binding.tvActiveRecurringCount.text = getString(R.string.active_recurring_count, activeCount)
        binding.tvMonthlyRecurringTotal.text = "${MoneyUtils.formatCents(monthlyTotalCents, currencySymbol)} / ${getString(R.string.freq_monthly)}"

        if (upcoming7DaysCount > 0) {
            binding.llUpcomingAlert.visibility = View.VISIBLE
            binding.tvUpcomingAlertText.text = getString(R.string.upcoming_alert_desc)
            binding.tvUpcomingAlertAmount.text = MoneyUtils.formatCents(upcoming7DaysAmount, currencySymbol)
        } else {
            binding.llUpcomingAlert.visibility = View.GONE
        }
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            "ALL" -> allItems
            else -> allItems.filter { it.frequency.equals(currentFilter, ignoreCase = true) }
        }

        adapter.submitList(filtered, currencySymbol)

        if (filtered.isEmpty()) {
            binding.rvRecurring.visibility = View.GONE
            binding.llEmptyRecurring.visibility = View.VISIBLE
        } else {
            binding.rvRecurring.visibility = View.VISIBLE
            binding.llEmptyRecurring.visibility = View.GONE
        }
    }

    private fun confirmDelete(item: RecurringTransactionEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_recurring_title))
            .setMessage(getString(R.string.confirm_delete_recurring_msg, item.title))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    app.repository.deleteRecurring(item)
                    Toast.makeText(this@RecurringActivity, getString(R.string.tx_deleted_success), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}

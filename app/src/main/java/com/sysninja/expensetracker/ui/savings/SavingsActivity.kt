package com.sysninja.expensetracker.ui.savings

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
import com.sysninja.expensetracker.data.local.entity.SavingsGoalEntity
import com.sysninja.expensetracker.databinding.ActivitySavingsBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SavingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavingsBinding
    private lateinit var adapter: SavingsGoalAdapter
    private var currencySymbol: String = "৳"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = SavingsGoalAdapter(
            currencySymbol = currencySymbol,
            onGoalClick = { goal ->
                val intent = Intent(this, SavingsDetailActivity::class.java).apply {
                    putExtra("goal_id", goal.id)
                }
                startActivity(intent)
            },
            onQuickDeposit = { goal ->
                showQuickDepositDialog(goal)
            }
        )
        binding.rvSavingsGoals.layoutManager = LinearLayoutManager(this)
        binding.rvSavingsGoals.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddNewGoal.setOnClickListener {
            startActivity(Intent(this, AddEditSavingsGoalActivity::class.java))
        }
    }

    private fun observeData() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            currencySymbol = app.preferencesManager.currencySymbolFlow.first()
            app.repository.getAllSavingsGoals().collect { goals ->
                updateOverview(goals)
                adapter.submitList(goals, currencySymbol)

                if (goals.isEmpty()) {
                    binding.rvSavingsGoals.visibility = View.GONE
                    binding.llEmptySavings.visibility = View.VISIBLE
                    binding.tvGoalsCountBadge.text = "০টি"
                } else {
                    binding.rvSavingsGoals.visibility = View.VISIBLE
                    binding.llEmptySavings.visibility = View.GONE
                    binding.tvGoalsCountBadge.text = "${goals.size}টি"
                }
            }
        }
    }

    private fun updateOverview(goals: List<SavingsGoalEntity>) {
        var totalSavedCents = 0L
        var totalTargetCents = 0L
        var completedCount = 0

        goals.forEach { g ->
            totalSavedCents += g.currentAmountCents
            totalTargetCents += g.targetAmountCents
            if (g.isCompleted || (g.targetAmountCents > 0 && g.currentAmountCents >= g.targetAmountCents)) {
                completedCount++
            }
        }

        val activeCount = goals.size - completedCount
        binding.tvTotalSavedAmount.text = MoneyUtils.formatCents(totalSavedCents, currencySymbol)
        binding.tvTotalTarget.text = MoneyUtils.formatCents(totalTargetCents, currencySymbol)
        binding.tvGoalsStatusInfo.text = "${activeCount}টি চলমান • ${completedCount}টি অর্জিত"

        val pct = if (totalTargetCents > 0) {
            ((totalSavedCents.toDouble() / totalTargetCents.toDouble()) * 100).toInt()
        } else {
            0
        }
        val clampedPct = pct.coerceIn(0, 100)
        binding.progressBarMasterSavings.progress = clampedPct
        binding.tvSavedPercentage.text = "$clampedPct% অর্জিত"
    }

    private fun showQuickDepositDialog(goal: SavingsGoalEntity) {
        val input = EditText(this).apply {
            hint = "টাকার পরিমাণ লিখুন (Amount)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_muted))
            setPadding(40, 30, 40, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("টাকা জমা করুন • Deposit to ${goal.name}")
            .setMessage("বর্তমান জমার পরিমাণ: ${MoneyUtils.formatCents(goal.currentAmountCents, currencySymbol)}")
            .setView(input)
            .setPositiveButton("জমা দিন") { _, _ ->
                val amtStr = input.text.toString().trim()
                val parsed = MoneyUtils.parseToCents(amtStr)
                if (parsed != null && parsed > 0) {
                    val app = application as ExpenseTrackerApp
                    lifecycleScope.launch {
                        val newTotal = goal.currentAmountCents + parsed
                        val isComplete = newTotal >= goal.targetAmountCents
                        app.repository.updateSavingsGoal(
                            goal.copy(
                                currentAmountCents = newTotal,
                                isCompleted = isComplete
                            )
                        )
                        Toast.makeText(this@SavingsActivity, "সফলভাবে জমা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "সঠিক পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("বাতিল", null)
            .show()
    }
}

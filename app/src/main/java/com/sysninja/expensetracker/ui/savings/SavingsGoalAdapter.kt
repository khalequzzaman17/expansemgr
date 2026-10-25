package com.sysninja.expensetracker.ui.savings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.SavingsGoalEntity
import com.sysninja.expensetracker.databinding.ItemSavingsGoalCardBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavingsGoalAdapter(
    private var currencySymbol: String = "৳",
    private val onGoalClick: (SavingsGoalEntity) -> Unit,
    private val onQuickDeposit: (SavingsGoalEntity) -> Unit
) : RecyclerView.Adapter<SavingsGoalAdapter.SavingsGoalViewHolder>() {

    private val goals = mutableListOf<SavingsGoalEntity>()

    fun submitList(newGoals: List<SavingsGoalEntity>, currency: String = currencySymbol) {
        goals.clear()
        goals.addAll(newGoals)
        currencySymbol = currency
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingsGoalViewHolder {
        val binding = ItemSavingsGoalCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavingsGoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavingsGoalViewHolder, position: Int) {
        holder.bind(goals[position])
    }

    override fun getItemCount(): Int = goals.size

    inner class SavingsGoalViewHolder(
        private val binding: ItemSavingsGoalCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: SavingsGoalEntity) {
            val context = binding.root.context
            binding.tvGoalName.text = goal.name
            binding.tvGoalSubtitle.text = goal.notes ?: "Savings Milestone"

            val currentFormatted = MoneyUtils.formatCents(goal.currentAmountCents, currencySymbol)
            val targetFormatted = MoneyUtils.formatCents(goal.targetAmountCents, currencySymbol)
            binding.tvSavedAmount.text = currentFormatted
            binding.tvTargetAmount.text = " / $targetFormatted"

            val pct = if (goal.targetAmountCents > 0) {
                ((goal.currentAmountCents.toDouble() / goal.targetAmountCents.toDouble()) * 100).toInt()
            } else {
                0
            }
            val clampedPct = pct.coerceIn(0, 100)
            binding.progressBarGoal.progress = clampedPct
            binding.tvProgressPercentage.text = "$clampedPct% সম্পন্ন"

            if (goal.isCompleted || clampedPct >= 100) {
                binding.tvCompletionBadge.text = "✓ অর্জিত"
                binding.tvCompletionBadge.setTextColor(ContextCompat.getColor(context, R.color.teal_primary))
                binding.progressBarGoal.setIndicatorColor(ContextCompat.getColor(context, R.color.teal_primary))
            } else {
                binding.tvCompletionBadge.text = "চলমান"
                binding.tvCompletionBadge.setTextColor(ContextCompat.getColor(context, R.color.mint_secondary))
                binding.progressBarGoal.setIndicatorColor(ContextCompat.getColor(context, R.color.tertiary_lavender))
            }

            if (goal.targetDate != null && goal.targetDate > 0) {
                val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                binding.tvTargetDate.text = sdf.format(Date(goal.targetDate))
                val remainingCents = (goal.targetAmountCents - goal.currentAmountCents).coerceAtLeast(0)
                binding.tvRemainingInfo.text = "আর ${MoneyUtils.formatCents(remainingCents, currencySymbol)} বাকি"
            } else {
                binding.tvTargetDate.text = "কোন নির্দিষ্ট মেয়াদ নেই"
                val remainingCents = (goal.targetAmountCents - goal.currentAmountCents).coerceAtLeast(0)
                binding.tvRemainingInfo.text = "আর ${MoneyUtils.formatCents(remainingCents, currencySymbol)} বাকি"
            }

            binding.cardSavingsGoal.setOnClickListener { onGoalClick(goal) }
            binding.btnGoalDetail.setOnClickListener { onGoalClick(goal) }
            binding.btnQuickDeposit.setOnClickListener { onQuickDeposit(goal) }
        }
    }
}

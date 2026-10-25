package com.sysninja.expensetracker.ui.recurring

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.RecurringTransactionEntity
import com.sysninja.expensetracker.databinding.ItemRecurringCardBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RecurringAdapter(
    private var currencySymbol: String = "৳",
    private val onRecurringClick: (RecurringTransactionEntity) -> Unit,
    private val onTogglePause: (RecurringTransactionEntity) -> Unit,
    private val onDeleteRecurring: (RecurringTransactionEntity) -> Unit
) : RecyclerView.Adapter<RecurringAdapter.RecurringViewHolder>() {

    private val items = mutableListOf<RecurringTransactionEntity>()

    fun submitList(newItems: List<RecurringTransactionEntity>, currency: String = currencySymbol) {
        items.clear()
        items.addAll(newItems)
        currencySymbol = currency
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecurringViewHolder {
        val binding = ItemRecurringCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecurringViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecurringViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class RecurringViewHolder(
        private val binding: ItemRecurringCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecurringTransactionEntity) {
            val context = binding.root.context
            binding.tvRecurringTitle.text = item.title

            val freqText = when (item.frequency.uppercase()) {
                "DAILY" -> context.getString(R.string.freq_daily)
                "WEEKLY" -> context.getString(R.string.freq_weekly)
                "YEARLY" -> context.getString(R.string.freq_yearly)
                else -> context.getString(R.string.freq_monthly)
            }
            binding.tvFrequencyChip.text = freqText

            binding.tvRecurringSubtitle.text = item.note ?: "${item.type}"

            val isExpense = !item.type.equals("INCOME", ignoreCase = true)
            val amountFormatted = MoneyUtils.formatCents(item.amountCents, currencySymbol)
            if (isExpense) {
                binding.tvRecurringAmount.text = "-$amountFormatted"
                binding.tvRecurringAmount.setTextColor(ContextCompat.getColor(context, R.color.rose_error))
            } else {
                binding.tvRecurringAmount.text = "+$amountFormatted"
                binding.tvRecurringAmount.setTextColor(ContextCompat.getColor(context, R.color.mint_secondary))
            }

            if (item.isPaused) {
                binding.tvRecurringStatus.text = context.getString(R.string.status_paused)
                binding.tvRecurringStatus.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                binding.btnTogglePause.text = context.getString(R.string.resume_label)
            } else {
                binding.tvRecurringStatus.text = context.getString(R.string.status_active)
                binding.tvRecurringStatus.setTextColor(ContextCompat.getColor(context, R.color.teal_primary))
                binding.btnTogglePause.text = context.getString(R.string.pause_action)
            }

            // Next due date
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvNextDueDate.text = context.getString(R.string.next_due, sdf.format(Date(item.nextDueDate)))

            val diffMs = item.nextDueDate - System.currentTimeMillis()
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
            when {
                diffDays < 0 -> {
                    binding.tvDaysRemaining.text = context.getString(R.string.overdue_label)
                    binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.rose_error))
                }
                diffDays == 0L -> {
                    binding.tvDaysRemaining.text = context.getString(R.string.due_today)
                    binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.amber_primary))
                }
                else -> {
                    binding.tvDaysRemaining.text = context.getString(R.string.days_remaining, diffDays)
                    binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.amber_primary))
                }
            }

            // MFS / Bank badge
            binding.tvAccountTag.text = item.paymentMethod

            binding.cardRecurring.setOnClickListener { onRecurringClick(item) }
            binding.btnTogglePause.setOnClickListener { onTogglePause(item) }
            binding.btnDeleteRecurring.setOnClickListener { onDeleteRecurring(item) }
        }
    }
}

package com.expensetracker.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.expensetracker.R
import com.expensetracker.data.model.TransactionWithDetails
import com.expensetracker.databinding.ItemTransactionBinding
import com.expensetracker.domain.DateUtils
import com.expensetracker.domain.MoneyUtils

class TransactionListAdapter(
    private var currencySymbol: String = "৳",
    private val onItemClick: (TransactionWithDetails) -> Unit
) : ListAdapter<TransactionWithDetails, TransactionListAdapter.ViewHolder>(DIFF_CALLBACK) {

    fun updateCurrency(newSymbol: String) {
        currencySymbol = newSymbol
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionWithDetails) {
            val tx = item.transaction
            val context = binding.root.context

            binding.tvTitle.text = tx.title
            binding.tvDate.text = DateUtils.formatDateTime(tx.timestamp)

            val amountStr = MoneyUtils.formatCents(tx.amountCents, currencySymbol)

            when (tx.type) {
                "INCOME" -> {
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_income)
                    binding.ivTypeIcon.setImageResource(R.drawable.ic_income)
                    binding.ivTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.income_green))
                    binding.tvAmount.text = "+ $amountStr"
                    binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green))
                    val catName = item.category?.name ?: context.getString(R.string.type_income)
                    val accName = item.account?.name ?: ""
                    binding.tvSubtitle.text = if (accName.isNotBlank()) "$catName • $accName" else catName
                }
                "EXPENSE" -> {
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_expense)
                    binding.ivTypeIcon.setImageResource(R.drawable.ic_expense)
                    binding.ivTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.expense_red))
                    binding.tvAmount.text = "- $amountStr"
                    binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red))
                    val catName = item.category?.name ?: context.getString(R.string.type_expense)
                    val accName = item.account?.name ?: ""
                    binding.tvSubtitle.text = if (accName.isNotBlank()) "$catName • $accName" else catName
                }
                "TRANSFER" -> {
                    binding.iconContainer.setBackgroundResource(R.drawable.bg_badge_transfer)
                    binding.ivTypeIcon.setImageResource(R.drawable.ic_transfer)
                    binding.ivTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.transfer_blue))
                    binding.tvAmount.text = "⇄ $amountStr"
                    binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.transfer_blue))
                    val fromName = item.account?.name ?: "Source"
                    val toName = item.toAccount?.name ?: "Destination"
                    binding.tvSubtitle.text = "$fromName → $toName"
                }
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionWithDetails>() {
            override fun areItemsTheSame(oldItem: TransactionWithDetails, newItem: TransactionWithDetails): Boolean {
                return oldItem.transaction.id == newItem.transaction.id
            }

            override fun areContentsTheSame(oldItem: TransactionWithDetails, newItem: TransactionWithDetails): Boolean {
                return oldItem == newItem
            }
        }
    }
}

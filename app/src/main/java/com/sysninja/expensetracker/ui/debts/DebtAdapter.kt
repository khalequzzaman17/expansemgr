package com.sysninja.expensetracker.ui.debts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.DebtEntity
import com.sysninja.expensetracker.databinding.ItemDebtCardBinding
import com.sysninja.expensetracker.domain.MoneyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DebtAdapter(
    private var currencySymbol: String = "৳",
    private val onDebtClick: (DebtEntity) -> Unit,
    private val onRecordSettlement: (DebtEntity) -> Unit,
    private val onDeleteDebt: (DebtEntity) -> Unit
) : RecyclerView.Adapter<DebtAdapter.DebtViewHolder>() {

    private val debts = mutableListOf<DebtEntity>()

    fun submitList(newDebts: List<DebtEntity>, currency: String = currencySymbol) {
        debts.clear()
        debts.addAll(newDebts)
        currencySymbol = currency
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val binding = ItemDebtCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DebtViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        holder.bind(debts[position])
    }

    override fun getItemCount(): Int = debts.size

    inner class DebtViewHolder(
        private val binding: ItemDebtCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(debt: DebtEntity) {
            val context = binding.root.context
            val isReceivable = debt.type.equals("RECEIVABLE", ignoreCase = true)
            val isPaid = debt.status.equals("PAID", ignoreCase = true) || debt.outstandingBalanceCents <= 0L

            // Initials
            val words = debt.personName.trim().split(" ")
            val initials = if (words.size >= 2) {
                "${words[0].firstOrNull() ?: ""}${words[1].firstOrNull() ?: ""}".uppercase()
            } else {
                debt.personName.take(2).uppercase()
            }
            binding.tvAvatarInitials.text = initials

            binding.tvPersonName.text = debt.personName
            binding.tvDebtNotes.text = debt.notes ?: if (isReceivable) "ধার দেওয়া হয়েছে" else "ধার নেওয়া হয়েছে"

            if (isReceivable) {
                binding.tvDebtTypeTag.text = "আমি পাব"
                binding.tvDebtTypeTag.setTextColor(ContextCompat.getColor(context, R.color.mint_secondary))
                binding.tvDebtAmount.setTextColor(ContextCompat.getColor(context, R.color.mint_secondary))
                binding.tvDebtAmount.text = "+${MoneyUtils.formatCents(debt.outstandingBalanceCents, currencySymbol)}"
                binding.btnRecordSettlement.text = "পরিশোধ গ্রহণ"
            } else {
                binding.tvDebtTypeTag.text = "আমি দেব"
                binding.tvDebtTypeTag.setTextColor(ContextCompat.getColor(context, R.color.rose_error))
                binding.tvDebtAmount.setTextColor(ContextCompat.getColor(context, R.color.rose_error))
                binding.tvDebtAmount.text = "-${MoneyUtils.formatCents(debt.outstandingBalanceCents, currencySymbol)}"
                binding.btnRecordSettlement.text = "পরিশোধ দিন"
            }

            if (isPaid) {
                binding.tvStatusChip.text = "পরিশোধিত • Settled"
                binding.tvStatusChip.setTextColor(ContextCompat.getColor(context, R.color.teal_primary))
                binding.llProgressContainer.visibility = android.view.View.GONE
                binding.btnRecordSettlement.visibility = android.view.View.GONE
            } else {
                binding.tvStatusChip.text = "চলমান • Active"
                binding.tvStatusChip.setTextColor(ContextCompat.getColor(context, R.color.tertiary_lavender))
                binding.llProgressContainer.visibility = android.view.View.VISIBLE
                binding.btnRecordSettlement.visibility = android.view.View.VISIBLE

                val original = debt.originalAmountCents
                val outstanding = debt.outstandingBalanceCents
                val paid = (original - outstanding).coerceAtLeast(0L)
                val pct = if (original > 0) ((paid.toDouble() / original.toDouble()) * 100).toInt() else 0
                val clamped = pct.coerceIn(0, 100)

                binding.progressBarDebt.progress = clamped
                binding.tvPaidProgress.text = "${MoneyUtils.formatCents(paid, currencySymbol)} পরিশোধিত ($clamped%)"
                binding.tvRemainingDebt.text = "${MoneyUtils.formatCents(outstanding, currencySymbol)} বাকি"
            }

            if (debt.dueDate != null && debt.dueDate > 0) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.tvDueDate.text = "মেয়াদ: ${sdf.format(Date(debt.dueDate))}"
            } else {
                binding.tvDueDate.text = "নির্দিষ্ট কোনো মেয়াদ নেই"
            }

            binding.cardDebt.setOnClickListener { onDebtClick(debt) }
            binding.btnRecordSettlement.setOnClickListener { onRecordSettlement(debt) }
            binding.btnDeleteDebt.setOnClickListener { onDeleteDebt(debt) }
        }
    }
}

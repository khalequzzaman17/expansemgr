package com.sysninja.expensetracker.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.FragmentTransactionsBinding
import com.sysninja.expensetracker.ui.addtransaction.AddEditTransactionActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var adapter: TransactionListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndFilters()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = TransactionListAdapter("৳") { item ->
            val intent = Intent(requireContext(), AddEditTransactionActivity::class.java).apply {
                putExtra("EXTRA_TRANSACTION_ID", item.transaction.id)
            }
            startActivity(intent)
        }

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TransactionsFragment.adapter
        }
    }

    private fun setupSearchAndFilters() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchQuery.value = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            val type = when {
                checkedIds.contains(R.id.chipExpense) -> "EXPENSE"
                checkedIds.contains(R.id.chipIncome) -> "INCOME"
                checkedIds.contains(R.id.chipTransfer) -> "TRANSFER"
                else -> null
            }
            viewModel.filterType.value = type
        }

        binding.ivSort.setOnClickListener { v ->
            val popup = PopupMenu(requireContext(), v)
            popup.menu.add(0, 1, 0, getString(R.string.sort_newest))
            popup.menu.add(0, 2, 1, getString(R.string.sort_oldest))
            popup.menu.add(0, 3, 2, getString(R.string.sort_highest))
            popup.menu.add(0, 4, 3, getString(R.string.sort_lowest))
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> viewModel.sortOrder.value = "NEWEST"
                    2 -> viewModel.sortOrder.value = "OLDEST"
                    3 -> viewModel.sortOrder.value = "HIGHEST"
                    4 -> viewModel.sortOrder.value = "LOWEST"
                }
                true
            }
            popup.show()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currency = viewModel.preferences.currencySymbolFlow.first()
            adapter.updateCurrency(currency)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.transactions.collect { list ->
                    binding.tvTransactionCount.text = getString(R.string.accounts_summary) + ": ${list.size}"
                    if (list.isEmpty()) {
                        binding.rvTransactions.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvTransactions.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                        adapter.submitList(list)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

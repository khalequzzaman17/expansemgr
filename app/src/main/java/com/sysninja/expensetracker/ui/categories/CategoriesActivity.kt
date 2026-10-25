package com.sysninja.expensetracker.ui.categories

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.CategoryEntity
import com.sysninja.expensetracker.databinding.ActivityCategoriesBinding
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var adapter: CategoryAdapter
    private var allCategories = listOf<CategoryEntity>()
    private var selectedType = "EXPENSE"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupListeners()
        observeData()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onCategoryClick = { cat ->
                openEditCategory(cat)
            },
            onEditClick = { cat ->
                openEditCategory(cat)
            },
            onDeleteClick = { cat ->
                confirmDelete(cat)
            }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddNewCategory.setOnClickListener {
            startActivity(Intent(this, AddEditCategoryActivity::class.java))
        }

        binding.chipGroupCategoryType.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedType = if (checkedIds.contains(binding.chipIncomeCategories.id)) {
                "INCOME"
            } else {
                "EXPENSE"
            }
            filterCategories()
        }

        binding.etSearchCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterCategories()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeData() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            app.repository.getAllActiveCategories().collect { categories ->
                allCategories = categories
                binding.tvCategorySummary.text = getString(R.string.categories_summary, categories.size)
                filterCategories()
            }
        }
    }

    private fun filterCategories() {
        val filtered = allCategories.filter { cat ->
            val matchesType = cat.type.equals(selectedType, ignoreCase = true)
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                cat.name.contains(searchQuery, ignoreCase = true)
            } else {
                true
            }
            matchesType && matchesSearch
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            binding.rvCategories.visibility = View.GONE
            binding.llEmptyCategories.visibility = View.VISIBLE
        } else {
            binding.rvCategories.visibility = View.VISIBLE
            binding.llEmptyCategories.visibility = View.GONE
        }
    }

    private fun openEditCategory(category: CategoryEntity) {
        val intent = Intent(this, AddEditCategoryActivity::class.java).apply {
            putExtra("category_id", category.id)
        }
        startActivity(intent)
    }

    private fun confirmDelete(category: CategoryEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_cat_title))
            .setMessage(getString(R.string.confirm_delete_cat_msg, category.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    app.repository.deleteCategory(category)
                    Toast.makeText(this@CategoriesActivity, getString(R.string.tx_deleted_success), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}

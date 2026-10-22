package com.sysninja.expensetracker.ui.budgets

import com.sysninja.expensetracker.ui.dp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.BudgetEntity
import com.sysninja.expensetracker.data.local.entity.CategoryEntity
import com.sysninja.expensetracker.domain.DateUtils
import com.sysninja.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEditBudgetActivity : AppCompatActivity() {

    private var categories: List<CategoryEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AddEditBudgetActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_budget)
            setTitleTextColor(ContextCompat.getColor(this@AddEditBudgetActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AddEditBudgetActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AddEditBudgetActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val spinnerCategory = Spinner(this).apply {
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(spinnerCategory)

        val limitEt = EditText(this).apply {
            hint = getString(R.string.budget_limit)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(ContextCompat.getColor(this@AddEditBudgetActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditBudgetActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        form.addView(limitEt)

        val saveBtn = MaterialButton(this).apply {
            text = getString(R.string.save)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@AddEditBudgetActivity, R.color.text_inverse))
            setOnClickListener {
                val limitCents = MoneyUtils.parseToCents(limitEt.text.toString())
                if (limitCents <= 0L) {
                    Toast.makeText(this@AddEditBudgetActivity, getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedCat = categories.getOrNull(spinnerCategory.selectedItemPosition)
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val monthKey = DateUtils.toMonthKey(System.currentTimeMillis())
                    val budget = BudgetEntity(
                        categoryId = selectedCat?.id,
                        monthKey = monthKey,
                        limitAmountCents = limitCents
                    )
                    app.repository.insertBudget(budget)
                    Toast.makeText(this@AddEditBudgetActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        form.addView(saveBtn)

        root.addView(form)
        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            categories = app.repository.getCategoriesByType("EXPENSE").first()
            val catNames = categories.map { it.name }
            spinnerCategory.adapter = ArrayAdapter(
                this@AddEditBudgetActivity,
                android.R.layout.simple_spinner_dropdown_item,
                catNames
            )
        }
    }
}

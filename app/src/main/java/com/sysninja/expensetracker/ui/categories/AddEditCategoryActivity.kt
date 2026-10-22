package com.sysninja.expensetracker.ui.categories

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
import com.sysninja.expensetracker.data.local.entity.CategoryEntity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AddEditCategoryActivity : AppCompatActivity() {

    private val categoryTypes = listOf("EXPENSE", "INCOME")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AddEditCategoryActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_category)
            setTitleTextColor(ContextCompat.getColor(this@AddEditCategoryActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AddEditCategoryActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AddEditCategoryActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val nameEt = EditText(this).apply {
            hint = getString(R.string.category_name)
            setTextColor(ContextCompat.getColor(this@AddEditCategoryActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditCategoryActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(nameEt)

        val spinnerType = Spinner(this).apply {
            adapter = ArrayAdapter(this@AddEditCategoryActivity, android.R.layout.simple_spinner_dropdown_item, categoryTypes)
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        form.addView(spinnerType)

        val saveBtn = MaterialButton(this).apply {
            text = getString(R.string.save)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@AddEditCategoryActivity, R.color.text_inverse))
            setOnClickListener {
                val name = nameEt.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(this@AddEditCategoryActivity, getString(R.string.error_required), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val type = categoryTypes[spinnerType.selectedItemPosition]

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val cat = CategoryEntity(
                        name = name,
                        type = type,
                        iconResName = "ic_category",
                        colorHex = if (type == "EXPENSE") "#F43F5E" else "#10B981",
                        isDefault = false
                    )
                    app.repository.insertCategory(cat)
                    Toast.makeText(this@AddEditCategoryActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        form.addView(saveBtn)

        root.addView(form)
        setContentView(root)
    }
}

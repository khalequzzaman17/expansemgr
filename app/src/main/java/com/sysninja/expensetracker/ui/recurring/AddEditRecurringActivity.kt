package com.sysninja.expensetracker.ui.recurring

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
import com.sysninja.expensetracker.data.local.entity.AccountEntity
import com.sysninja.expensetracker.data.local.entity.RecurringTransactionEntity
import com.sysninja.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEditRecurringActivity : AppCompatActivity() {

    private val frequencies = listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY")
    private var accounts: List<AccountEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_recurring)
            setTitleTextColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val titleEt = EditText(this).apply {
            hint = getString(R.string.hint_title)
            setTextColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(titleEt)

        val amountEt = EditText(this).apply {
            hint = getString(R.string.hint_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(amountEt)

        val spinnerFreq = Spinner(this).apply {
            adapter = ArrayAdapter(this@AddEditRecurringActivity, android.R.layout.simple_spinner_dropdown_item, frequencies)
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(spinnerFreq)

        val spinnerAcc = Spinner(this).apply {
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        form.addView(spinnerAcc)

        val saveBtn = MaterialButton(this).apply {
            text = getString(R.string.save)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@AddEditRecurringActivity, R.color.text_inverse))
            setOnClickListener {
                val title = titleEt.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(this@AddEditRecurringActivity, getString(R.string.error_required), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val amtCents = MoneyUtils.parseToCents(amountEt.text.toString())
                if (amtCents <= 0L) {
                    Toast.makeText(this@AddEditRecurringActivity, getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val freq = frequencies[spinnerFreq.selectedItemPosition]
                val acc = accounts.getOrNull(spinnerAcc.selectedItemPosition) ?: accounts.firstOrNull()
                if (acc == null) return@setOnClickListener

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val recurring = RecurringTransactionEntity(
                        title = title,
                        amountCents = amtCents,
                        type = "EXPENSE",
                        accountId = acc.id,
                        frequency = freq,
                        startDate = System.currentTimeMillis(),
                        nextDueDate = System.currentTimeMillis() + 86400000L
                    )
                    app.repository.insertRecurring(recurring)
                    Toast.makeText(this@AddEditRecurringActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        form.addView(saveBtn)

        root.addView(form)
        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            accounts = app.repository.getAllActiveAccounts().first()
            val names = accounts.map { it.name }
            spinnerAcc.adapter = ArrayAdapter(
                this@AddEditRecurringActivity,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
        }
    }
}

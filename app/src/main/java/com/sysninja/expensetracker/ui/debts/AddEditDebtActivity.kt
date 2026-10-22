package com.sysninja.expensetracker.ui.debts

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
import com.sysninja.expensetracker.data.local.entity.DebtEntity
import com.sysninja.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AddEditDebtActivity : AppCompatActivity() {

    private val debtTypes = listOf("PAYABLE", "RECEIVABLE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_debt)
            setTitleTextColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AddEditDebtActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val personEt = EditText(this).apply {
            hint = getString(R.string.person_organization)
            setTextColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(personEt)

        val amountEt = EditText(this).apply {
            hint = getString(R.string.hint_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(amountEt)

        val spinnerType = Spinner(this).apply {
            adapter = ArrayAdapter(this@AddEditDebtActivity, android.R.layout.simple_spinner_dropdown_item, debtTypes)
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        form.addView(spinnerType)

        val saveBtn = MaterialButton(this).apply {
            text = getString(R.string.save)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@AddEditDebtActivity, R.color.text_inverse))
            setOnClickListener {
                val person = personEt.text.toString().trim()
                if (person.isBlank()) {
                    Toast.makeText(this@AddEditDebtActivity, getString(R.string.error_required), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val amtCents = MoneyUtils.parseToCents(amountEt.text.toString())
                if (amtCents <= 0L) {
                    Toast.makeText(this@AddEditDebtActivity, getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val type = debtTypes[spinnerType.selectedItemPosition]

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val debt = DebtEntity(
                        type = type,
                        personName = person,
                        originalAmountCents = amtCents,
                        outstandingBalanceCents = amtCents
                    )
                    app.repository.insertDebt(debt)
                    Toast.makeText(this@AddEditDebtActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        form.addView(saveBtn)

        root.addView(form)
        setContentView(root)
    }
}

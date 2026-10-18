package com.expensetracker.ui.accounts

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.R
import com.expensetracker.data.local.entity.AccountEntity
import com.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AddEditAccountActivity : AppCompatActivity() {

    private val accountTypes = listOf("CASH", "BANK", "BKASH", "NAGAD", "CARD", "SAVINGS", "OTHER")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_account)
            setTitleTextColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AddEditAccountActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val nameEt = EditText(this).apply {
            hint = getString(R.string.account_name)
            setTextColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(nameEt)

        val balanceEt = EditText(this).apply {
            hint = getString(R.string.initial_balance)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(balanceEt)

        val spinnerType = Spinner(this).apply {
            adapter = ArrayAdapter(this@AddEditAccountActivity, android.R.layout.simple_spinner_dropdown_item, accountTypes)
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
            setBackgroundColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@AddEditAccountActivity, R.color.text_inverse))
            setOnClickListener {
                val name = nameEt.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(this@AddEditAccountActivity, getString(R.string.error_required), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val initialBal = MoneyUtils.parseToCents(balanceEt.text.toString())
                val type = accountTypes[spinnerType.selectedItemPosition]

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val account = AccountEntity(
                        name = name,
                        type = type,
                        initialBalanceCents = initialBal,
                        colorHex = "#10B981",
                        iconName = "ic_wallet"
                    )
                    app.repository.insertAccount(account)
                    Toast.makeText(this@AddEditAccountActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        form.addView(saveBtn)

        root.addView(form)
        setContentView(root)
    }
}

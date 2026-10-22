package com.sysninja.expensetracker.ui.savings

import com.sysninja.expensetracker.ui.dp

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.data.local.entity.SavingsGoalEntity
import com.sysninja.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class AddEditSavingsGoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_savings_goal)
            setTitleTextColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val nameEt = EditText(this).apply {
            hint = getString(R.string.goal_name)
            setTextColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        form.addView(nameEt)

        val targetEt = EditText(this).apply {
            hint = getString(R.string.target_amount)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        form.addView(targetEt)

        val saveBtn = MaterialButton(this).apply {
            text = getString(R.string.save)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@AddEditSavingsGoalActivity, R.color.text_inverse))
            setOnClickListener {
                val name = nameEt.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(this@AddEditSavingsGoalActivity, getString(R.string.error_required), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val targetCents = MoneyUtils.parseToCents(targetEt.text.toString())
                if (targetCents <= 0L) {
                    Toast.makeText(this@AddEditSavingsGoalActivity, getString(R.string.error_invalid_amount), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val goal = SavingsGoalEntity(
                        name = name,
                        targetAmountCents = targetCents,
                        currentAmountCents = 0L,
                        targetDate = System.currentTimeMillis() + 86400000L * 90
                    )
                    app.repository.insertSavingsGoal(goal)
                    Toast.makeText(this@AddEditSavingsGoalActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        form.addView(saveBtn)

        root.addView(form)
        setContentView(root)
    }
}

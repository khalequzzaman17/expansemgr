package com.sysninja.expensetracker.ui.savings

import com.sysninja.expensetracker.ui.dp

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.domain.MoneyUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SavingsDetailActivity : AppCompatActivity() {

    private var goalId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goalId = intent.getLongExtra("EXTRA_GOAL_ID", 0L)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.savings_title)
            setTitleTextColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@SavingsDetailActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        val titleTv = TextView(this).apply {
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.text_primary))
            paint.isFakeBoldText = true
        }
        content.addView(titleTv)

        val progressTv = TextView(this).apply {
            textSize = 15f
            setTextColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.teal_light))
            setPadding(0, 12, 0, 24)
        }
        content.addView(progressTv)

        val depositEt = EditText(this).apply {
            hint = getString(R.string.contribution_amount_hint)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.text_muted))
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(dp(16), dp(12), dp(16), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        content.addView(depositEt)

        val depositBtn = MaterialButton(this).apply {
            text = getString(R.string.add_deposit)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@SavingsDetailActivity, R.color.text_inverse))
            setOnClickListener {
                val amt = MoneyUtils.parseToCents(depositEt.text.toString())
                if (amt <= 0L) return@setOnClickListener

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val goal = app.repository.getSavingsGoalById(goalId) ?: return@launch
                    app.repository.updateSavingsGoal(goal.copy(currentAmountCents = goal.currentAmountCents + amt))
                    Toast.makeText(this@SavingsDetailActivity, getString(R.string.contribution_recorded), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        content.addView(depositBtn)

        root.addView(content)
        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            val goal = app.repository.getSavingsGoalById(goalId) ?: return@launch
            titleTv.text = goal.name
            progressTv.text = "${MoneyUtils.formatCents(goal.currentAmountCents, currency)} / ${MoneyUtils.formatCents(goal.targetAmountCents, currency)}"
        }
    }
}

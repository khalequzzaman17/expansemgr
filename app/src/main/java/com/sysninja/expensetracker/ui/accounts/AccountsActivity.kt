package com.sysninja.expensetracker.ui.accounts

import com.sysninja.expensetracker.ui.dp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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

class AccountsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@AccountsActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.accounts_title)
            setTitleTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@AccountsActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@AccountsActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val actionsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(12), dp(16), dp(8))
        }

        val addBtn = MaterialButton(this).apply {
            text = getString(R.string.add_account)
            setBackgroundResource(R.drawable.bg_button_teal)
            setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.text_inverse))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 12
            }
            setOnClickListener {
                startActivity(Intent(this@AccountsActivity, AddEditAccountActivity::class.java))
            }
        }
        actionsLayout.addView(addBtn)

        val transferBtn = MaterialButton(this).apply {
            text = getString(R.string.transfer_money)
            setBackgroundColor(ContextCompat.getColor(this@AccountsActivity, R.color.surface_navy_elevated))
            setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.transfer_blue))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                startActivity(Intent(this@AccountsActivity, TransferActivity::class.java))
            }
        }
        actionsLayout.addView(transferBtn)
        root.addView(actionsLayout)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(16))
        }
        scrollView.addView(listContainer)
        root.addView(scrollView)

        setContentView(root)

        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val currency = app.preferencesManager.currencySymbolFlow.first()
            app.repository.getAccountBalances().collect { balances ->
                listContainer.removeAllViews()
                balances.forEach { item ->
                    val card = LinearLayout(this@AccountsActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(dp(16), dp(16), dp(16), dp(16))
                        setBackgroundResource(R.drawable.bg_card_rounded)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = 20 }
                    }

                    val nameTv = TextView(this@AccountsActivity).apply {
                        text = "${item.accountName} (${item.accountType})"
                        setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.text_primary))
                        textSize = 16f
                        paint.isFakeBoldText = true
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }
                    card.addView(nameTv)

                    val balTv = TextView(this@AccountsActivity).apply {
                        text = MoneyUtils.formatCents(item.balanceCents, currency)
                        setTextColor(ContextCompat.getColor(this@AccountsActivity, R.color.teal_light))
                        textSize = 16f
                        paint.isFakeBoldText = true
                    }
                    card.addView(balTv)

                    listContainer.addView(card)
                }
            }
        }
    }
}

package com.sysninja.expensetracker.ui.security

import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.domain.SecurityManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class SetPinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@SetPinActivity, R.color.bg_navy_dark))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.set_pin)
            setTitleTextColor(ContextCompat.getColor(this@SetPinActivity, R.color.text_primary))
            setBackgroundColor(ContextCompat.getColor(this@SetPinActivity, R.color.surface_navy))
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationIconTint(ContextCompat.getColor(this@SetPinActivity, R.color.text_primary))
            setNavigationOnClickListener { finish() }
        }
        root.addView(toolbar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val pinEt = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@SetPinActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@SetPinActivity, R.color.text_muted))
            hint = "Enter 4-Digit PIN"
            maxLines = 1
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        content.addView(pinEt)

        val confirmEt = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            gravity = Gravity.CENTER
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@SetPinActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@SetPinActivity, R.color.text_muted))
            hint = "Confirm 4-Digit PIN"
            maxLines = 1
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
        }
        content.addView(confirmEt)

        val saveBtn = MaterialButton(this).apply {
            text = getString(R.string.save)
            setBackgroundColor(ContextCompat.getColor(this@SetPinActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@SetPinActivity, R.color.text_inverse))
            setOnClickListener {
                val pin1 = pinEt.text.toString().trim()
                val pin2 = confirmEt.text.toString().trim()
                if (pin1.length < 4) {
                    Toast.makeText(this@SetPinActivity, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (pin1 != pin2) {
                    Toast.makeText(this@SetPinActivity, getString(R.string.pin_mismatch), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val salt = SecurityManager.generateSalt()
                    val hash = SecurityManager.hashPin(pin1, salt)
                    app.preferencesManager.setPin(hash, salt)
                    SecurityManager.isSessionUnlocked = true
                    Toast.makeText(this@SetPinActivity, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        content.addView(saveBtn)

        val disableBtn = MaterialButton(this).apply {
            text = "Disable PIN Lock"
            setBackgroundColor(ContextCompat.getColor(this@SetPinActivity, R.color.surface_navy_elevated))
            setTextColor(ContextCompat.getColor(this@SetPinActivity, R.color.expense_red))
            setOnClickListener {
                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    app.preferencesManager.disablePin()
                    Toast.makeText(this@SetPinActivity, "PIN lock disabled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        content.addView(disableBtn)

        root.addView(content)
        setContentView(root)
    }
}

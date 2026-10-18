package com.expensetracker.ui.security

import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.expensetracker.ExpenseTrackerApp
import com.expensetracker.R
import com.expensetracker.domain.SecurityManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PinLockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@PinLockActivity, R.color.bg_navy_dark))
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val titleTv = TextView(this).apply {
            text = getString(R.string.enter_pin)
            setTextColor(ContextCompat.getColor(this@PinLockActivity, R.color.text_primary))
            textSize = 20f
            paint.isFakeBoldText = true
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        root.addView(titleTv)

        val pinEt = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            gravity = Gravity.CENTER
            textSize = 24f
            setTextColor(ContextCompat.getColor(this@PinLockActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@PinLockActivity, R.color.text_muted))
            hint = "••••"
            maxLines = 1
            setBackgroundResource(R.drawable.bg_edit_text)
            setPadding(32, 24, 32, 24)
            layoutParams = LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 32
            }
        }
        root.addView(pinEt)

        val unlockBtn = MaterialButton(this).apply {
            text = "Unlock"
            setBackgroundColor(ContextCompat.getColor(this@PinLockActivity, R.color.teal_primary))
            setTextColor(ContextCompat.getColor(this@PinLockActivity, R.color.text_inverse))
            setOnClickListener {
                val enteredPin = pinEt.text.toString().trim()
                if (enteredPin.length < 4) {
                    Toast.makeText(this@PinLockActivity, "Please enter 4 digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val app = application as ExpenseTrackerApp
                lifecycleScope.launch {
                    val hash = app.preferencesManager.pinHashFlow.first()
                    val salt = app.preferencesManager.pinSaltFlow.first()
                    if (SecurityManager.verifyPin(enteredPin, hash, salt)) {
                        finish()
                    } else {
                        Toast.makeText(this@PinLockActivity, getString(R.string.pin_wrong, SecurityManager.getRemainingAttempts()), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        root.addView(unlockBtn)

        setContentView(root)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back press from bypassing PIN
        finishAffinity()
    }
}

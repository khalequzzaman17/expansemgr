package com.sysninja.expensetracker.ui.security

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityPinLockBinding
import com.sysninja.expensetracker.domain.SecurityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class PinLockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinLockBinding
    private val pinBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupKeypad()
        setupBiometric()

        binding.btnForgotPin.setOnClickListener {
            Toast.makeText(this, "অনুগ্রহ করে আপনার মাস্টার সিকিউরিটি ব্যাকআপ ব্যবহার করুন", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupKeypad() {
        val numberKeys = listOf(
            binding.key0 to "0",
            binding.key1 to "1",
            binding.key2 to "2",
            binding.key3 to "3",
            binding.key4 to "4",
            binding.key5 to "5",
            binding.key6 to "6",
            binding.key7 to "7",
            binding.key8 to "8",
            binding.key9 to "9"
        )

        numberKeys.forEach { (view, digit) ->
            view.setOnClickListener {
                if (pinBuilder.length < 4) {
                    pinBuilder.append(digit)
                    updatePinDots()
                    if (pinBuilder.length == 4) {
                        verifyEnteredPin(pinBuilder.toString())
                    }
                }
            }
        }

        binding.keyBackspace.setOnClickListener {
            if (pinBuilder.isNotEmpty()) {
                pinBuilder.deleteCharAt(pinBuilder.length - 1)
                updatePinDots()
            }
        }
    }

    private fun updatePinDots() {
        val len = pinBuilder.length
        binding.pinDot1.setBackgroundResource(if (len >= 1) R.drawable.bg_button_teal else R.drawable.bg_pill_sync)
        binding.pinDot2.setBackgroundResource(if (len >= 2) R.drawable.bg_button_teal else R.drawable.bg_pill_sync)
        binding.pinDot3.setBackgroundResource(if (len >= 3) R.drawable.bg_button_teal else R.drawable.bg_pill_sync)
        binding.pinDot4.setBackgroundResource(if (len >= 4) R.drawable.bg_button_teal else R.drawable.bg_pill_sync)
    }

    private fun verifyEnteredPin(pin: String) {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val hash = app.preferencesManager.pinHashFlow.first()
            val salt = app.preferencesManager.pinSaltFlow.first()
            if (SecurityManager.verifyPin(pin, hash, salt)) {
                SecurityManager.isSessionUnlocked = true
                finish()
            } else {
                Toast.makeText(this@PinLockActivity, getString(R.string.pin_wrong, SecurityManager.getRemainingAttempts()), Toast.LENGTH_SHORT).show()
                pinBuilder.clear()
                updatePinDots()
            }
        }
    }

    private fun setupBiometric() {
        binding.keyBiometric.setOnClickListener {
            promptBiometric()
        }

        // Automatically prompt biometric if available
        promptBiometric()
    }

    private fun promptBiometric() {
        try {
            val executor: Executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    SecurityManager.isSessionUnlocked = true
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("বায়োমেট্রিক আনলক • Biometric Unlock")
                .setSubtitle("হিসাব খাতা খুলতে আঙুলের ছাপ দিন")
                .setNegativeButtonText("পিন ব্যবহার করুন")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (_: Exception) {
            // Biometric not available on device or not configured
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAffinity()
    }
}

package com.sysninja.expensetracker.ui.security

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
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
    private var lockoutTimer: CountDownTimer? = null
    private var keyViews: List<View> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prevent screen capture / recents view of PIN entry
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupKeypad()
        checkLockoutStatus()
        setupBiometric()

        binding.btnForgotPin.setOnClickListener {
            Toast.makeText(this, getString(R.string.pin_forgot_hint), Toast.LENGTH_LONG).show()
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
        keyViews = numberKeys.map { it.first } + binding.keyBackspace

        numberKeys.forEach { (view, digit) ->
            view.setOnClickListener {
                if (SecurityManager.isLockedOut()) return@setOnClickListener
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
            if (SecurityManager.isLockedOut()) return@setOnClickListener
            if (pinBuilder.isNotEmpty()) {
                pinBuilder.deleteCharAt(pinBuilder.length - 1)
                updatePinDots()
            }
        }
    }

    private fun setKeypadEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1.0f else 0.4f
        keyViews.forEach {
            it.isEnabled = enabled
            it.alpha = alpha
        }
    }

    private fun checkLockoutStatus() {
        if (SecurityManager.isLockedOut()) {
            val seconds = SecurityManager.getRemainingLockoutSeconds()
            startLockoutCountdown(seconds)
        } else {
            setKeypadEnabled(true)
            binding.tvPinHint.text = getString(R.string.pin_security_prompt)
            binding.tvPinHint.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun startLockoutCountdown(totalSeconds: Int) {
        setKeypadEnabled(false)
        pinBuilder.clear()
        updatePinDots()
        lockoutTimer?.cancel()

        lockoutTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val s = ((millisUntilFinished / 1000L) + 1).toInt()
                binding.tvPinHint.text = getString(R.string.too_many_attempts_lockout, s)
                binding.tvPinHint.setTextColor(ContextCompat.getColor(this@PinLockActivity, R.color.expense_red))
            }

            override fun onFinish() {
                setKeypadEnabled(true)
                binding.tvPinHint.text = getString(R.string.pin_security_prompt)
                binding.tvPinHint.setTextColor(ContextCompat.getColor(this@PinLockActivity, R.color.text_secondary))
            }
        }.start()
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
                if (SecurityManager.isLockedOut()) {
                    startLockoutCountdown(SecurityManager.getRemainingLockoutSeconds())
                } else {
                    Toast.makeText(
                        this@PinLockActivity,
                        getString(R.string.pin_wrong, SecurityManager.getRemainingAttempts()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                pinBuilder.clear()
                updatePinDots()
            }
        }
    }

    private fun setupBiometric() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val isBioEnabled = app.preferencesManager.isBiometricEnabledFlow.first()
            if (isBioEnabled) {
                binding.keyBiometric.visibility = View.VISIBLE
                binding.keyBiometric.setOnClickListener {
                    if (!SecurityManager.isLockedOut()) {
                        promptBiometric()
                    }
                }
                // Automatically prompt biometric if available and user enabled it
                if (!SecurityManager.isLockedOut()) {
                    promptBiometric()
                }
            } else {
                binding.keyBiometric.visibility = View.INVISIBLE
            }
        }
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
                .setTitle(getString(R.string.biometric_unlock_title))
                .setSubtitle(getString(R.string.biometric_unlock_desc))
                .setNegativeButtonText(getString(R.string.use_pin_instead))
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (_: Exception) {
            // Biometric not available on device or not configured
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lockoutTimer?.cancel()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishAffinity()
    }
}

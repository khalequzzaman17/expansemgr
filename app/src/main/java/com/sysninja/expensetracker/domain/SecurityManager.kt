package com.sysninja.expensetracker.domain

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityManager {

    private const val SALT_LENGTH = 16
    private var failedAttempts = 0
    private var lockoutUntilTimestamp: Long = 0L

    var isSessionUnlocked = false

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$pin:$salt".toByteArray(Charsets.UTF_8)
        val digest = md.digest(combined)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    fun verifyPin(enteredPin: String, storedHash: String, storedSalt: String): Boolean {
        if (isLockedOut()) return false
        val computedHash = hashPin(enteredPin, storedSalt)
        val matches = (computedHash == storedHash)
        if (matches) {
            failedAttempts = 0
            lockoutUntilTimestamp = 0L
            isSessionUnlocked = true
        } else {
            failedAttempts++
            if (failedAttempts >= 5) {
                // 30 second lockout
                lockoutUntilTimestamp = System.currentTimeMillis() + 30_000L
            }
        }
        return matches
    }

    fun isLockedOut(): Boolean {
        val now = System.currentTimeMillis()
        if (now < lockoutUntilTimestamp) {
            return true
        }
        if (lockoutUntilTimestamp > 0L && now >= lockoutUntilTimestamp) {
            // Lockout expired
            lockoutUntilTimestamp = 0L
            failedAttempts = 0
        }
        return false
    }

    fun getRemainingLockoutSeconds(): Int {
        val remaining = (lockoutUntilTimestamp - System.currentTimeMillis()) / 1000L
        return if (remaining > 0L) remaining.toInt() else 0
    }

    fun getRemainingAttempts(): Int {
        return (5 - failedAttempts).coerceAtLeast(0)
    }

    fun resetSession() {
        isSessionUnlocked = false
    }
}

package com.sysninja.expensetracker.domain

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object SecurityManager {

    private const val SALT_LENGTH = 16
    private const val HASH_ITERATIONS = 5000
    private var failedAttempts = 0
    private var lockoutUntilTimestamp: Long = 0L

    @Volatile
    var isSessionUnlocked = false

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /**
     * Hashes PIN with salt using SHA-256 over multiple iterations for brute-force resistance.
     */
    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        var current = "$pin:$salt".toByteArray(Charsets.UTF_8)
        repeat(HASH_ITERATIONS) {
            current = md.digest(current)
        }
        return Base64.encodeToString(current, Base64.NO_WRAP)
    }

    /**
     * Legacy single-iteration hash for backward compatibility.
     */
    private fun legacyHashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$pin:$salt".toByteArray(Charsets.UTF_8)
        val digest = md.digest(combined)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    /**
     * Verifies entered PIN using constant-time comparison to prevent timing attacks.
     */
    fun verifyPin(enteredPin: String, storedHash: String, storedSalt: String): Boolean {
        if (isLockedOut()) return false

        val computedIterated = hashPin(enteredPin, storedSalt)
        val computedLegacy = legacyHashPin(enteredPin, storedSalt)

        val matchesIterated = MessageDigest.isEqual(
            computedIterated.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )

        val matchesLegacy = MessageDigest.isEqual(
            computedLegacy.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )

        val matches = matchesIterated || matchesLegacy

        if (matches) {
            failedAttempts = 0
            lockoutUntilTimestamp = 0L
            isSessionUnlocked = true
        } else {
            failedAttempts++
            if (failedAttempts >= 5) {
                // 30 second exponential lockout
                val lockoutDuration = if (failedAttempts > 7) 60_000L else 30_000L
                lockoutUntilTimestamp = System.currentTimeMillis() + lockoutDuration
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

    fun lockSession() {
        isSessionUnlocked = false
    }

    fun resetSession() {
        isSessionUnlocked = false
        failedAttempts = 0
        lockoutUntilTimestamp = 0L
    }
}

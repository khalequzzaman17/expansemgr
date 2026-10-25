package com.sysninja.expensetracker.domain

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.ui.security.PinLockActivity
import com.sysninja.expensetracker.ui.security.SetPinActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppLockLifecycleManager(private val app: ExpenseTrackerApp) : Application.ActivityLifecycleCallbacks {

    private var foregroundActivityCount = 0
    private var isChangingConfigurations = false
    private var lastBackgroundTimestamp = 0L

    companion object {
        private const val GRACE_PERIOD_MS = 1500L // Brief grace period for orientation change/camera pickers
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Enforce active locale synchronously on activity resources
        LocaleManager.applyLocaleDirect(activity, LocaleManager.getPersistedLanguage(activity))

        // Enforce FLAG_SECURE on sensitive security activities
        if (activity is PinLockActivity || activity is SetPinActivity) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (foregroundActivityCount == 0 && !isChangingConfigurations) {
            val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
            if (lastBackgroundTimestamp != 0L && elapsed > GRACE_PERIOD_MS) {
                // App came back from background, re-lock session
                SecurityManager.lockSession()
            }
        }
        foregroundActivityCount++
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is PinLockActivity) return

        CoroutineScope(Dispatchers.Main).launch {
            val isPinEnabled = app.preferencesManager.isPinEnabledFlow.first()
            if (isPinEnabled) {
                // Ensure window is protected from recents/screenshots
                activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
                if (!SecurityManager.isSessionUnlocked) {
                    val intent = Intent(activity, PinLockActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    activity.startActivity(intent)
                }
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        isChangingConfigurations = activity.isChangingConfigurations
        foregroundActivityCount--
        if (foregroundActivityCount <= 0 && !isChangingConfigurations) {
            foregroundActivityCount = 0
            lastBackgroundTimestamp = System.currentTimeMillis()
            SecurityManager.lockSession()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}

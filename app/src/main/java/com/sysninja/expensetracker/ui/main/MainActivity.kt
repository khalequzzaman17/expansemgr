package com.sysninja.expensetracker.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sysninja.expensetracker.ExpenseTrackerApp
import com.sysninja.expensetracker.R
import com.sysninja.expensetracker.databinding.ActivityMainBinding
import com.sysninja.expensetracker.domain.SecurityManager
import com.sysninja.expensetracker.ui.addtransaction.AddEditTransactionActivity
import com.sysninja.expensetracker.ui.dashboard.DashboardFragment
import com.sysninja.expensetracker.ui.security.PinLockActivity
import com.sysninja.expensetracker.ui.reports.ReportsActivity
import com.sysninja.expensetracker.ui.settings.SettingsActivity
import com.sysninja.expensetracker.ui.transactions.TransactionsFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPinLock()

        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment(), "HOME")
        }

        setupBottomNavigation()
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditTransactionActivity::class.java))
        }
    }

    private fun checkPinLock() {
        val app = application as ExpenseTrackerApp
        lifecycleScope.launch {
            val isPinEnabled = app.preferencesManager.isPinEnabledFlow.first()
            if (isPinEnabled && !SecurityManager.isSessionUnlocked) {
                val intent = Intent(this@MainActivity, PinLockActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(DashboardFragment(), "HOME")
                    true
                }
                R.id.nav_transactions -> {
                    replaceFragment(TransactionsFragment(), "TRANSACTIONS")
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    false
                }
                R.id.nav_more -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    false
                }
                else -> false
            }
        }
    }

    fun selectTransactionsTab() {
        binding.bottomNavigation.selectedItemId = R.id.nav_transactions
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }
}

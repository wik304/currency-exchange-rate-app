package com.example.currencyexchangerateapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.currencyexchangerateapp.data.SettingsManager
import com.example.currencyexchangerateapp.ui.navigation.Navigation
import com.example.currencyexchangerateapp.ui.theme.CurrencyExchangeRateAppTheme
import com.example.currencyexchangerateapp.worker.HistoryWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    val settingsManager = SettingsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val currentKey = settingsManager.getApiKey()

        if (currentKey.isEmpty()) {
            settingsManager.saveApiKey("5a9cf9cbf83844c7b0635d69")
        }

        setupDailyWorker()

        lifecycleScope.launch {
            settingsManager.getAutoRefresh().collect { isEnabled ->
                settingsManager.getRefreshInterval().collect { interval ->
                    if (isEnabled) {
                        setupHistoryWorker(interval.toLong())
                    } else {
                        cancelHistoryWorker()
                    }
                }
            }
        }

        setContent {
            CurrencyExchangeRateAppTheme {
                Navigation(settingsManager)
            }
        }
    }

    fun setupHistoryWorker(intervalMinutes: Long) {
        val finalInterval = if (intervalMinutes < 15) 15L else intervalMinutes

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val historyWorkRequest = PeriodicWorkRequestBuilder<HistoryWorker>(
            finalInterval, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CurrencyHistoryWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            historyWorkRequest
        )
    }

    fun cancelHistoryWorker() {
        WorkManager.getInstance(this).cancelUniqueWork("CurrencyHistoryWork")
    }

    fun setupDailyWorker() {
        val dailyRequest = PeriodicWorkRequestBuilder<HistoryWorker>(
            24, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyHistoryUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )
    }
}
package com.example.currencyexchangerateapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.currencyexchangerateapp.ui.theme.CurrencyExchangeRateAppTheme
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

        setupHistoryWorker(1440)

        setContent {
            CurrencyExchangeRateAppTheme {
                Navigation(settingsManager)
            }
        }
    }

    fun setupHistoryWorker(intervalMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val historyWorkRequest = PeriodicWorkRequestBuilder<HistoryWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(settingsManager.context).enqueueUniquePeriodicWork(
            "CurrencyHistoryWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            historyWorkRequest
        )
    }
}
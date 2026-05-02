package com.example.currencyexchangerateapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.currencyexchangerateapp.data.SettingsManager
import com.example.currencyexchangerateapp.viewmodel.RetrofitInstance

class HistoryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    private val targetCurrencies = listOf(
        "PLN", "USD", "EUR", "GBP", "CHF", "JPY", "AUD", "CAD", "CNY", "HKD",
        "NZD", "SEK", "KRW", "SGD", "NOK"
    )

    override suspend fun doWork(): Result {
        val settingsManager = SettingsManager(applicationContext)
        val key = settingsManager.getApiKey()

        if (key.isEmpty()) {
            return Result.failure()
        }

        var allSuccessful = true

        for (baseCurrency in targetCurrencies) {
            try {
                val response = RetrofitInstance.api.getRates(key, baseCurrency)

                if (response.result == "success") {
                    val filteredRates = response.conversionRates.filterKeys {
                        it in targetCurrencies && it != baseCurrency
                    }

                    settingsManager.saveToHistory(baseCurrency, filteredRates)
                } else {
                    allSuccessful = false
                }
            } catch (_: Exception) {
                allSuccessful = false
            }
        }

        return if (allSuccessful) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
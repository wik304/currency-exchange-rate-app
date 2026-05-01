package com.example.currencyexchangerateapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RefreshSource {
    NETWORK_CARD,
    MANUAL_CARD
}

data class MainScreenState(
    val isNetworkLoading: Boolean = false,
    val isManualLoading: Boolean = false,
    val rates: Map<String, Double>? = null,
    val errorMessage: String? = null,
    val isOfflineMode: Boolean = false
)

data class ExchangeRateResponse(
    val result: String,
    @SerializedName("base_code")
    val baseCode: String,
    @SerializedName("conversion_rates")
    val conversionRates: Map<String, Double>
)

interface ExchangeRateApi {
    @GET("v6/{apiKey}/latest/{baseCurrency}")
    suspend fun getRates(
        @Path("apiKey") apiKey: String,
        @Path("baseCurrency") baseCurrency: String
    ): ExchangeRateResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://v6.exchangerate-api.com/"

    val api: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApi::class.java)
    }
}

class MainViewModel(val settingsManager: SettingsManager) : ViewModel() {
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.getBaseCurrency().collect { newCurrency ->
                getRatesForCurrency(
                    newCurrency,
                    isAutomatic = true
                )
            }
        }
    }

    fun getRatesForCurrency(
        currency: String,
        source: RefreshSource = RefreshSource.MANUAL_CARD,
        isAutomatic: Boolean = false
    ) {
        val key = settingsManager.getApiKey()

        if (key.isEmpty()) {
            _state.value = _state.value.copy(
                errorMessage = "No API key founded"
            )
            return
        }

        if (!isAutomatic) {
            _state.value = when (source) {
                RefreshSource.NETWORK_CARD -> _state.value.copy(
                    isNetworkLoading = true,
                    errorMessage = null
                )

                RefreshSource.MANUAL_CARD -> _state.value.copy(
                    isManualLoading = true,
                    errorMessage = null
                )
            }
        }

        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()

            val lastUpdate = settingsManager.getLastUpdateTime(currency)
            val tenMinutesInMillis = 10 * 60 * 1000

            val isCacheOld = (currentTime - lastUpdate) > tenMinutesInMillis

            val cachedRatesJson = settingsManager.getCachedRates(currency)

            try {
                if (isCacheOld || cachedRatesJson.isEmpty() || source == RefreshSource.MANUAL_CARD) {

                    val response = RetrofitInstance.api.getRates(key, currency)

                    if (response.result == "success") {
                        val jsonToSave = Gson().toJson(response.conversionRates)

                        settingsManager.saveCachedRates(currency, jsonToSave)
                        settingsManager.saveLastUpdateTime(currency, currentTime)
                        settingsManager.saveToHistory(currency, response.conversionRates)

                        _state.value = _state.value.copy(
                            isNetworkLoading = false,
                            isManualLoading = false,
                            rates = response.conversionRates,
                            isOfflineMode = false
                        )
                    } else {
                        loadFromCache(cachedRatesJson)
                    }
                } else {
                    loadFromCache(
                        cachedRatesJson,
                        isFresh = true
                    )
                }
            } catch (_: Exception) {
                loadFromCache(cachedRatesJson)
            }
        }
    }

    private fun loadFromCache(json: String, isFresh: Boolean = false) {
        if (json.isNotEmpty()) {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            val ratesFromCache: Map<String, Double> = Gson().fromJson(json, type)

            _state.value = _state.value.copy(
                isNetworkLoading = false, isManualLoading = false,
                rates = ratesFromCache,
                isOfflineMode = !isFresh,
                errorMessage = null
            )
        } else {
            _state.value = _state.value.copy(
                isNetworkLoading = false, isManualLoading = false,
                errorMessage = "No internet connection and no cached data."
            )
        }
    }

    fun getHistoryForCurrency(baseCurrency: String, targetCurrency: String, days: Int): List<Pair<String, Double>> {
        val file = File(settingsManager.context.filesDir, "currency_history.txt")

        if (!file.exists()) {
            return emptyList()
        }

        val allHistory = mutableListOf<Triple<String, String, Double>>()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        try {
            file.readLines().forEach { line ->
                if (line.isBlank()) return@forEach

                val parts = line.split(" | ")
                if (parts.size >= 3 && parts[1].trim() == baseCurrency) {
                    val dateTimeFull = parts[0].trim()
                    val date: String
                    val time: String

                    if (dateTimeFull.contains(" ")) {
                        val dt = dateTimeFull.split(" ")
                        date = dt[0]
                        time = dt[1]
                    } else {
                        date = dateTimeFull
                        time = "00:00"
                    }

                    val ratesString = parts[2]
                    val rate = ratesString.substringAfter("$targetCurrency=")
                        .substringBefore(",")
                        .substringBefore("}")
                        .toDoubleOrNull()

                    if (rate != null) {
                        allHistory.add(Triple(date, time, rate))
                    }
                }
            }

            if (allHistory.isEmpty()) return emptyList()

            return if (days == 1) {
                val filtered = allHistory.filter { it.first == today }
                filtered.map { it.second to it.third }
            } else {
                allHistory.groupBy { it.first }
                    .mapNotNull { entry ->
                        val firstElement = entry.value.firstOrNull() ?: return@mapNotNull null
                        entry.key to firstElement.third
                    }
                    .sortedBy { it.first }
                    .takeLast(days)
                    .map { (date, rate) ->
                        val dateParts = date.split("-")
                        val formattedDate = if (dateParts.size >= 3) "${dateParts[2]}.${dateParts[1]}" else date
                        formattedDate to rate
                    }
            }
        } catch (e: Exception) {
            android.util.Log.e("CURRENCY_APP", "Error parsing history", e)
            return emptyList()
        }
    }
}

class MainViewModelFactory(
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

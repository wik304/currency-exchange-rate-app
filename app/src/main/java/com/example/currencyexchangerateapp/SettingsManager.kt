package com.example.currencyexchangerateapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(val context: Context) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(key: String) = securePrefs.edit().putString("api_key", key).apply()
    fun getApiKey(): String = securePrefs.getString("api_key", "") ?: ""

    companion object {
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
        val IS_AUTO_REFRESH_ENABLED = booleanPreferencesKey("is_auto_refresh")
        val REFRESH_INTERVAL = floatPreferencesKey("refresh_interval")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")

        val FAVOURITE_CURRENCIES = stringPreferencesKey("favourite_currencies")
    }

    suspend fun saveFavourites(currencies: Set<String>) {
        context.dataStore.edit { it[FAVOURITE_CURRENCIES] = currencies.joinToString(",") }
    }

    fun getFavourites(): Flow<Set<String>> = context.dataStore.data.map {
        val prefs = it[FAVOURITE_CURRENCIES] ?: "PLN,USD,EUR,GBP,CHF"
        prefs.split(",").filter { s -> s.isNotEmpty() }.toSet()
    }

    suspend fun saveBaseCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_CURRENCY] = currency
        }
    }

    fun getBaseCurrency(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BASE_CURRENCY] ?: "PLN"
    }

    suspend fun saveAutoRefreshStatus(enabled: Boolean) {
        context.dataStore.edit { it[IS_AUTO_REFRESH_ENABLED] = enabled }
    }

    fun getAutoRefresh(): Flow<Boolean> = context.dataStore.data.map {
        it[IS_AUTO_REFRESH_ENABLED] ?: false
    }

    suspend fun saveRefreshInterval(minutes: Float) {
        context.dataStore.edit { it[REFRESH_INTERVAL] = minutes }
    }

    fun getRefreshInterval(): Flow<Float> = context.dataStore.data.map {
        it[REFRESH_INTERVAL] ?: 15f
    }


    suspend fun saveDecimalPlaces(places: Int) {
        context.dataStore.edit { it[DECIMAL_PLACES] = places }
    }

    fun getDecimalPlaces(): Flow<Int> = context.dataStore.data.map {
        it[DECIMAL_PLACES] ?: 4
    }

    fun saveLastUpdateTime(currency: String, time: Long) {
        securePrefs.edit()
            .putLong("last_update_$currency", time)
            .apply()
    }

    fun getLastUpdateTime(currency: String): Long =
        securePrefs.getLong("last_update_$currency", 0L)

    fun saveCachedRates(currency: String, ratesJson: String) {
        securePrefs.edit()
            .putString("cached_rates_$currency", ratesJson)
            .apply()
    }

    fun getCachedRates(currency: String): String =
        securePrefs.getString("cached_rates_$currency", "") ?: ""

    fun saveToHistory(base: String, rates: Map<String, Double>) {
        val dateFull = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val file = File(context.filesDir, "currency_history.txt")
        val lines = if (file.exists()) file.readLines().toMutableList() else mutableListOf()

        val existingIndex = lines.indexOfFirst { it.startsWith("$dateFull | $base") }
        val newEntry = "$dateFull | $base | $rates"

        if (existingIndex != -1) {
            lines[existingIndex] = newEntry
        } else {
            lines.add(newEntry)
        }
        file.writeText(lines.joinToString("\n") + "\n")
    }


}

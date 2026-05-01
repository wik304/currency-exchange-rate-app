package com.example.currencyexchangerateapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    val state by viewModel.state.collectAsState()

    val baseCurrency by settingsManager.getBaseCurrency().collectAsState("PLN")
    val decimalPlaces by settingsManager.getDecimalPlaces().collectAsState(4)

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (state.isOfflineMode) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Offline Mode - Displaying cached data",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (state.rates != null) {
            val currencyNames = mapOf(
                "PLN" to "Polish Zloty",
                "USD" to "United States Dollar",
                "EUR" to "Euro",
                "GBP" to "British Pound",
                "CHF" to "Swiss Franc",
                "JPY" to "Japanese Yen",
                "AUD" to "Australian Dollar",
                "CAD" to "Canadian Dollar",
                "CNY" to "Chinese Yuan",
                "HKD" to "Hong Kong Dollar",
                "NZD" to "New Zealand Dollar",
                "SEK" to "Swedish Krona",
                "KRW" to "South Korean Won",
                "SGD" to "Singapore Dollar",
                "NOK" to "Norwegian Krone",
            )

            val filteredListToDisplay = state.rates!!
                .filterKeys { it in currencyNames.keys && it != baseCurrency }
                .toList()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredListToDisplay) { (currencyCode, rate) ->
                    val rate = state.rates?.get(currencyCode) ?: 0.0
                    val history = viewModel.getHistoryForCurrency(baseCurrency, currencyCode, 2)

                    val previousRate = history.getOrNull(0)?.second ?: rate
                    val changePercent = if (previousRate != 0.0) ((rate - previousRate) / previousRate) * 100 else 0.0

                    CurrencyItem(
                        decimalPlaces = decimalPlaces,
                        currencyCode = currencyCode,
                        rate = rate,
                        changePercent = changePercent,
                        fullName = currencyNames[currencyCode] ?: "Unknown",
                        modifier = Modifier.clickable {
                            navController.navigate("details/$currencyCode")
                        }
                    )
                }
            }
        } else {
            Spacer(
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun getFlagUrl(currencyCode: String): String {
    val countryCode = when (currencyCode) {
        "USD" -> "us"
        "EUR" -> "eu"
        "GBP" -> "gb"
        "CHF" -> "ch"
        "PLN" -> "pl"
        "JPY" -> "jp"
        "AUD" -> "au"
        "CAD" -> "ca"
        "CNY" -> "cn"
        "HKD" -> "hk"
        "NZD" -> "nz"
        "SEK" -> "se"
        "KRW" -> "kr"
        "SGD" -> "sg"
        "NOK" -> "no"
        else -> "unknown"
    }
    return "https://flagcdn.com/w40/$countryCode.png"
}

@Composable
fun CurrencyItem(
    decimalPlaces: Int,
    currencyCode: String,
    rate: Double,
    changePercent: Double,
    fullName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        val color = when {
            changePercent > 0 -> Color(0xFF4CAF50)
            changePercent < 0 -> Color(0xFFF44336)
            else -> Color.Gray
        }

        val icon = when {
            changePercent > 0 -> "▲"
            changePercent < 0 -> "▼"
            else -> "●"
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = getFlagUrl(currencyCode),
                contentDescription = "Flag $fullName",
                modifier = Modifier
                    .size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = currencyCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.${decimalPlaces}f", rate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = icon,
                        color = color,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = String.format("%+.2f%%", changePercent),
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

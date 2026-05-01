package com.example.currencyexchangerateapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun FavouriteScreen(
    navController: NavController,
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    val scope = rememberCoroutineScope()
    val favouriteCurrencies by settingsManager.getFavourites().collectAsState(initial = emptySet())

    val allCurrencies = listOf(
        "PLN", "USD", "EUR", "GBP", "CHF", "JPY", "AUD", "CAD", "CNY", "HKD", "NZD", "SEK", "KRW", "SGD", "NOK"
    )

    val currencyNames = mapOf(
        "PLN" to "Polish Zloty", "USD" to "United States Dollar", "EUR" to "Euro",
        "GBP" to "British Pound", "CHF" to "Swiss Franc", "JPY" to "Japanese Yen",
        "AUD" to "Australian Dollar", "CAD" to "Canadian Dollar", "CNY" to "Chinese Yuan",
        "HKD" to "Hong Kong Dollar", "NZD" to "New Zealand Dollar", "SEK" to "Swedish Krona",
        "KRW" to "South Korean Won", "SGD" to "Singapore Dollar", "NOK" to "Norwegian Krone"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Manage Currencies",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allCurrencies) { currencyCode ->
                val isFavourite = favouriteCurrencies.contains(currencyCode)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = getFlagUrl(currencyCode),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = currencyCode, fontWeight = FontWeight.Bold)
                                Text(text = currencyNames[currencyCode] ?: "", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Switch(
                            checked = isFavourite,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    val newFavourites = favouriteCurrencies.toMutableSet()
                                    if (checked) newFavourites.add(currencyCode)
                                    else newFavourites.remove(currencyCode)
                                    settingsManager.saveFavourites(newFavourites)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
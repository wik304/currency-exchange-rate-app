package com.example.currencyexchangerateapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.currencyexchangerateapp.viewmodel.MainViewModel
import com.example.currencyexchangerateapp.utils.NetworkMonitor
import com.example.currencyexchangerateapp.data.CurrencyData
import com.example.currencyexchangerateapp.data.SettingsManager

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    val state by viewModel.state.collectAsState()
    val baseCurrency by settingsManager.getBaseCurrency().collectAsState("PLN")
    val decimalPlaces by settingsManager.getDecimalPlaces().collectAsState(4)

    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isOnline by networkMonitor.isConnected.collectAsState(initial = true)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val screenWidthDp = configuration.smallestScreenWidthDp
    val columns = when {
        isLandscape && screenWidthDp >= 600 -> 3
        isLandscape -> 2
        screenWidthDp >= 600 -> 2
        else -> 1
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (!isLandscape) {
            Text(
                text = "Watched Currencies",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (!isOnline) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No internet connection - displaying cached data",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val favouriteCurrencies by settingsManager.getFavourites()
            .collectAsState(initial = emptySet())

        if (state.rates != null) {
            val filteredListToDisplay = state.rates!!
                .filterKeys {
                    it in CurrencyData.getAllCodes() &&
                            it != baseCurrency &&
                            it in favouriteCurrencies
                }
                .toList()

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredListToDisplay) { (currencyCode, rate) ->
                    val history = viewModel.getHistoryForCurrency(baseCurrency, currencyCode, 2)

                    val previousRate = history.getOrNull(0)?.second ?: rate
                    val changePercent =
                        if (previousRate != 0.0) ((rate - previousRate) / previousRate) * 100 else 0.0

                    CurrencyItem(
                        decimalPlaces = decimalPlaces,
                        currencyCode = currencyCode,
                        rate = rate,
                        changePercent = changePercent,
                        fullName = CurrencyData.getCurrencyName(currencyCode),
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
            .fillMaxWidth(),
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
                model = CurrencyData.getFlagUrl(currencyCode),
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

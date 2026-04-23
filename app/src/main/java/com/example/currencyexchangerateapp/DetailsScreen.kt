package com.example.currencyexchangerateapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DetailsScreen(
    currencyCode: String,
    navController: NavController,
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    val state by viewModel.state.collectAsState()
    var selectedDays by remember { mutableIntStateOf(7) }
    val history = viewModel.getHistoryForCurrency(currencyCode, selectedDays)
    val baseCurrency = state.rates?.keys?.firstOrNull() ?: "PLN"

    val currentRate = state.rates?.get(currencyCode) ?: 0.0
    val previousRate = history.getOrNull(0) ?: currentRate
    val change = currentRate - previousRate
    val changePercent = if (previousRate != 0.0) (change / previousRate) * 100 else 0.0

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
        "NOK" to "Norwegian Krone"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                ) {
                    AsyncImage(
                        model = getFlagUrl(baseCurrency),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = baseCurrency,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currencyNames[baseCurrency] ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = currencyCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currencyNames[currencyCode] ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AsyncImage(
                        model = getFlagUrl(currencyCode),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(7, 14, 30).forEach { days ->
                FilterChip(
                    selected = selectedDays == days,
                    onClick = { selectedDays = days },
                    label = { Text("$days days") }
                )
            }
        }

        if (history.size >= 2) {
            LineChart(
                data = history,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 16.dp)
            )
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("There is no historical data for this currency.")
            }
        }

        Spacer(modifier = Modifier.size(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format("%.4f", currentRate),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = String.format("%+.2f%%", changePercent),
                color = if (change >= 0) Color(0xFF4CAF50) else Color.Red,
                style = MaterialTheme.typography.titleMedium
            )
        }

        val date = SimpleDateFormat("dd.MM.yyyy HH:mm", LocalLocale.current.platformLocale).format(
            Date(settingsManager.getLastUpdateTime(currencyCode))
        )
        Text(text = "Updated: $date", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.weight(1f))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Data source:", fontWeight = FontWeight.Bold)
                Text("v6.exchangerate-api.com", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val max = data.maxOrNull() ?: 0.0
        val min = data.minOrNull() ?: 0.0
        val range = if (max - min == 0.0) 1.0 else max - min
        val width = size.width
        val height = size.height

        val points = data.mapIndexed { index, value ->
            Offset(
                x = index * (width / (data.size - 1)),
                y = height - ((value - min) / range * height).toFloat()
            )
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                start = points[i],
                end = points[i + 1],
                color = color,
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

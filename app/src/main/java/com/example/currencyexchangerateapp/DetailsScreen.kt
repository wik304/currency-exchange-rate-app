package com.example.currencyexchangerateapp

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
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
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
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
                    modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End
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

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                listOf(7, 14, 30).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text("$days days") })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(vertical = 8.dp)) {
                if (history.size >= 2) {
                    LineChart(
                        data = history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp), contentAlignment = Alignment.Center
                    ) {
                        Text("There is no historical data for this currency.\n.")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%.4f", currentRate),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = String.format("%+.2f%%", changePercent),
                        color = if (change >= 0) Color(0xFF4CAF50) else Color.Red,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                val lastUpdate = settingsManager.getLastUpdateTime(currencyCode)
                val date = if (lastUpdate > 0) {
                    SimpleDateFormat("dd.MM.yyyy HH:mm", LocalLocale.current.platformLocale).format(
                        Date(
                            lastUpdate
                        )
                    )
                } else "Never"

                Text(text = "Updated: $date", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Data source:", fontWeight = FontWeight.Bold)
                Text("v6.exchangerate-api.com", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LineChart(
    data: List<Double>, modifier: Modifier = Modifier
) {
    val model = remember(data) {
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(data.map { it.toFloat() })
            })
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            layers = arrayOf(rememberLineCartesianLayer()),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom()
        ), model = model, modifier = modifier
    )
}
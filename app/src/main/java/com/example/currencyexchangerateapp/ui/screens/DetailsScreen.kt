package com.example.currencyexchangerateapp.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.currencyexchangerateapp.viewmodel.MainViewModel
import com.example.currencyexchangerateapp.data.CurrencyData
import com.example.currencyexchangerateapp.data.SettingsManager
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis.Companion.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
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
    var selectedDays by remember { mutableIntStateOf(1) }
    val baseCurrency by settingsManager.getBaseCurrency().collectAsState("PLN")
    val history = viewModel.getHistoryForCurrency(baseCurrency, currencyCode, selectedDays)

    val currentRate = state.rates?.get(currencyCode) ?: 0.0
    val previousRate = history.getOrNull(0)?.second ?: currentRate
    val change = currentRate - previousRate
    val changePercent = if (previousRate != 0.0) (change / previousRate) * 100 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                        model = CurrencyData.getFlagUrl(baseCurrency),
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
                            text = CurrencyData.getCurrencyName(baseCurrency),
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
                            text = CurrencyData.getCurrencyName(currencyCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AsyncImage(
                        model = CurrencyData.getFlagUrl(currencyCode),
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
                listOf(1, 7, 30).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text(if (days == 1) "1 day" else "$days days") })
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
            Column(modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(vertical = 8.dp)) {
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
                        Text(
                            text = "There is no historical data for this currency.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(200.dp)
                        )
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

                val lastUpdate = settingsManager.getLastUpdateTime(baseCurrency)
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
    data: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    val values = data.map { it.second.toFloat() }

    val minY = values.minOrNull() ?: 0f
    val maxY = values.maxOrNull() ?: 1f

    val (finalMin, finalMax) = if (minY == maxY) {
        val delta = if (minY == 0f) 1f else minY * 0.1f
        (minY - delta) to (maxY + delta)
    } else {
        val range = maxY - minY
        val padding = range * 0.2f
        (minY - padding) to (maxY + padding)
    }

    val safeRange = (finalMax - finalMin).takeIf { it > 0f } ?: 1f
    val step = (safeRange / 4f).toDouble()

    val model = remember(data) {
        CartesianChartModel(
            LineCartesianLayerModel.build {
                series(data.map { it.second.toFloat() })
            }
        )
    }

    val bottomAxisValueFormatter = CartesianValueFormatter { _, x, _ ->
        val index = x.toInt()
        data.getOrNull(index)?.first ?: ""
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            layers = arrayOf(
                rememberLineCartesianLayer(
                    rangeProvider = CartesianLayerRangeProvider.fixed(
                        minY = finalMin.toDouble(),
                        maxY = finalMax.toDouble()
                    )
                )
            ),
            startAxis = rememberStart(
                itemPlacer = VerticalAxis.ItemPlacer.step(
                    step = { step }
                ),
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
                valueFormatter = CartesianValueFormatter { _, value, _ ->
                    String.format("%.4f", value)
                },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = bottomAxisValueFormatter
            ),
            marker = null
        ),
        model = model,
        modifier = modifier,
        zoomState = rememberVicoZoomState(zoomEnabled = false)
    )
}
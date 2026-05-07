package com.example.currencyexchangerateapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalConfiguration
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val chartHeight = when {
        isTablet && isLandscape -> 350.dp
        isTablet -> 450.dp
        isLandscape -> 180.dp
        else -> 250.dp
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
    ) {
        if (!isLandscape) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        CurrencyInfoCard(baseCurrency, currencyCode)
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        ChartFilterCard(selectedDays) { selectedDays = it }
                    }
                }
            } else {
                CurrencyInfoCard(baseCurrency, currencyCode)
                Spacer(modifier = Modifier.height(8.dp))
                ChartFilterCard(selectedDays) { selectedDays = it }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ChartContentCard(history, chartHeight)

            Spacer(modifier = Modifier.height(8.dp))

            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        CurrentRateCard(currentRate, changePercent, baseCurrency, settingsManager)

                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        DataSourceCard()
                    }
                }
            } else {
                CurrentRateCard(currentRate, changePercent, baseCurrency, settingsManager)
                Spacer(modifier = Modifier.height(8.dp))
                DataSourceCard()
            }

            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ChartFilterCard(selectedDays) { selectedDays = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    ChartContentCard(history, chartHeight)
                }

                Column(modifier = Modifier.weight(1f)) {
                    CurrencyInfoCard(baseCurrency, currencyCode)
                    Spacer(modifier = Modifier.height(8.dp))
                    CurrentRateCard(currentRate, changePercent, baseCurrency, settingsManager)
                    Spacer(modifier = Modifier.height(8.dp))
                    DataSourceCard()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CurrencyInfoCard(baseCurrency: String, currencyCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f)) {
                AsyncImage(model = CurrencyData.getFlagUrl(baseCurrency), contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(baseCurrency, fontWeight = FontWeight.Bold)
                    Text(CurrencyData.getCurrencyName(baseCurrency), style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(currencyCode, fontWeight = FontWeight.Bold)
                    Text(CurrencyData.getCurrencyName(currencyCode), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(12.dp))
                AsyncImage(model = CurrencyData.getFlagUrl(currencyCode), contentDescription = null, modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun ChartFilterCard(selectedDays: Int, onDaysSelected: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 7, 30).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { onDaysSelected(days) },
                        label = { Text(if (days == 1) "1 day" else "$days days") }
                    )
                }
            }
        }
    }
}

@Composable
fun ChartContentCard(history: List<Pair<String, Double>>, chartHeight: androidx.compose.ui.unit.Dp) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            if (history.size >= 2) {
                LineChart(data = history, modifier = Modifier.fillMaxWidth().height(chartHeight))
            } else {
                Box(Modifier.fillMaxWidth().height(chartHeight), contentAlignment = Alignment.Center) {
                    Text("There is no historical data for this currency.", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun CurrentRateCard(currentRate: Double, changePercent: Double, baseCurrency: String, settingsManager: SettingsManager) {
    Card(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(String.format("%.4f", currentRate), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(12.dp))
                val color = if (changePercent > 0) Color(0xFF4CAF50) else if (changePercent < 0) Color(0xFFF44336) else Color.Gray
                val icon = if (changePercent > 0) "▲" else if (changePercent < 0) "▼" else "●"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, color = color, modifier = Modifier.padding(end = 4.dp))
                    Text(String.format("%+.2f%%", changePercent), color = color, fontWeight = FontWeight.Medium)
                }
            }
            val lastUpdate = settingsManager.getLastUpdateTime(baseCurrency)
            val date = if (lastUpdate > 0) SimpleDateFormat("dd.MM.yyyy HH:mm", LocalLocale.current.platformLocale).format(Date(lastUpdate)) else "Never"
            Text(text = "Updated: $date", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DataSourceCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Data source:", fontWeight = FontWeight.Bold)
            Text("v6.exchangerate-api.com", style = MaterialTheme.typography.bodySmall)
        }
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
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}
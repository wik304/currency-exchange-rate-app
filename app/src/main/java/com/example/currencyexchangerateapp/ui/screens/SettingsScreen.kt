package com.example.currencyexchangerateapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.currencyexchangerateapp.viewmodel.MainViewModel
import com.example.currencyexchangerateapp.utils.NetworkMonitor
import com.example.currencyexchangerateapp.viewmodel.RefreshSource
import com.example.currencyexchangerateapp.data.CurrencyData
import com.example.currencyexchangerateapp.data.SettingsManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: MainViewModel = viewModel(),
    settingsManager: SettingsManager
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    val savedBaseCurrency by settingsManager.getBaseCurrency().collectAsState("PLN")
    val savedAutoRefresh by settingsManager.getAutoRefresh().collectAsState(false)
    val savedInterval by settingsManager.getRefreshInterval().collectAsState(15f)
    val savedDecimalPlaces by settingsManager.getDecimalPlaces().collectAsState(4)

    var baseCurrency by remember(savedBaseCurrency) { mutableStateOf(savedBaseCurrency) }
    var isAutoRefreshEnabled by remember(savedAutoRefresh) { mutableStateOf(savedAutoRefresh) }
    var refreshInterval by remember(savedInterval) { mutableFloatStateOf(savedInterval) }
    var decimalPlaces by remember(savedDecimalPlaces) { mutableIntStateOf(savedDecimalPlaces) }

    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val isNetworkConnected by networkMonitor.isConnected.collectAsState(initial = false)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val screenWidthDp = configuration.screenWidthDp
    val columns = when {
        isLandscape && screenWidthDp >= 600 -> 3
        isLandscape -> 2
        screenWidthDp >= 600 -> 2
        else -> 1
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (!isLandscape) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (columns >= 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConnectionStatusCard(
                        isNetworkConnected = isNetworkConnected,
                        isNetworkLoading = state.isNetworkLoading,
                        onRefreshClick = { viewModel.getRatesForCurrency(baseCurrency, RefreshSource.NETWORK_CARD) }
                    )
                    ManualRefreshCard(
                        isLoading = state.isManualLoading,
                        onRefreshClick = { viewModel.getRatesForCurrency(baseCurrency, RefreshSource.MANUAL_CARD) }
                    )
                    AutoRefreshCard(
                        isEnabled = isAutoRefreshEnabled,
                        refreshInterval = refreshInterval,
                        onEnabledChange = { isChecked ->
                            isAutoRefreshEnabled = isChecked
                            scope.launch { settingsManager.saveAutoRefreshStatus(isChecked) }
                        },
                        onIntervalChange = { newValue ->
                            refreshInterval = newValue
                            scope.launch { settingsManager.saveRefreshInterval(newValue.roundToInt().toFloat()) }
                        }
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BaseCurrencyCard(
                        baseCurrency = baseCurrency,
                        onBaseCurrencyChange = { newCode ->
                            baseCurrency = newCode
                            scope.launch { settingsManager.saveBaseCurrency(newCode) }
                        }
                    )
                    FormatCard(
                        decimalPlaces = decimalPlaces,
                        onDecimalPlacesChange = { newValue ->
                            decimalPlaces = newValue
                            scope.launch { settingsManager.saveDecimalPlaces(newValue) }
                        }
                    )
                }
            }
        } else {
            ConnectionStatusCard(isNetworkConnected, state.isNetworkLoading) {
                viewModel.getRatesForCurrency(baseCurrency, RefreshSource.NETWORK_CARD)
            }
            BaseCurrencyCard(baseCurrency) {
                baseCurrency = it
                scope.launch { settingsManager.saveBaseCurrency(it) }
            }
            FormatCard(decimalPlaces) {
                decimalPlaces = it
                scope.launch { settingsManager.saveDecimalPlaces(it) }
            }
            ManualRefreshCard(state.isManualLoading) {
                viewModel.getRatesForCurrency(baseCurrency, RefreshSource.MANUAL_CARD)
            }
            AutoRefreshCard(
                isEnabled = isAutoRefreshEnabled,
                refreshInterval = refreshInterval,
                onEnabledChange = { isChecked ->
                    isAutoRefreshEnabled = isChecked
                    scope.launch { settingsManager.saveAutoRefreshStatus(isChecked) }
                },
                onIntervalChange = { newValue ->
                    refreshInterval = newValue
                    scope.launch { settingsManager.saveRefreshInterval(newValue.roundToInt().toFloat()) }
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FormatCard(
    decimalPlaces: Int,
    onDecimalPlacesChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Display format",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Number of decimal places",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2, 3, 4).forEach { places ->
                    FilterChip(
                        selected = decimalPlaces == places,
                        onClick = { onDecimalPlacesChange(places) },
                        label = {
                            Text("0." + "0".repeat(places))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BaseCurrencyCard(
    baseCurrency: String,
    onBaseCurrencyChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Base currency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Exchange rates will be calculated relative to this currency.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                var expanded by remember { mutableStateOf(false) }
                val currencies = CurrencyData.currencies

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                    ) {
                        Text(baseCurrency)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency.code) },
                                onClick = {
                                    expanded = false
                                    onBaseCurrencyChange(currency.code)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutoRefreshCard(
    isEnabled: Boolean,
    refreshInterval: Float,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Automatic refresh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Download data in the background",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onEnabledChange(it) }
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                Text("Frequency: every ${refreshInterval.roundToInt()} min")

                Slider(
                    value = refreshInterval,
                    onValueChange = { onIntervalChange(it) },
                    onValueChangeFinished = { onIntervalChange(refreshInterval) },
                    valueRange = 15f..60f,
                    steps = 8
                )
            }
        }
    }
}

@Composable
fun ManualRefreshCard(
    isLoading: Boolean,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Manual refresh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Download data now",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh now"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isNetworkConnected: Boolean,
    isNetworkLoading: Boolean,
    onRefreshClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Connection status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isNetworkConnected) Color(0xFF4CAF50) else Color.Red
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isNetworkConnected) "Connected to the network" else "No connection",
                    )
                }

                IconButton(
                    onClick = onRefreshClick,
                    enabled = !isNetworkLoading
                ) {
                    if (isNetworkLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            }
        }
    }
}
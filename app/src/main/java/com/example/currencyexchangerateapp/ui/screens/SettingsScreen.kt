package com.example.currencyexchangerateapp.ui.screens

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

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
                                    if (isNetworkConnected) {
                                        Color(0xFF4CAF50)
                                    } else {
                                        Color.Red
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isNetworkConnected) {
                                "Connected to the network"
                            } else {
                                "No connection"
                            },
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.getRatesForCurrency(baseCurrency, RefreshSource.NETWORK_CARD)
                        },
                        enabled = !state.isNetworkLoading
                    ) {
                        if (state.isNetworkLoading) {
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

        Card(
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
                            onClick = {
                                expanded = true
                            },
                        ) {
                            Text(
                                baseCurrency
                            )
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
                                        baseCurrency = currency.code
                                        expanded = false
                                        scope.launch {
                                            settingsManager.saveBaseCurrency(currency.code)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
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
                    FilterChip(
                        selected = decimalPlaces == 2,
                        onClick = {
                            decimalPlaces = 2
                            scope.launch {
                                settingsManager.saveDecimalPlaces(2)
                            }
                        },
                        label = {
                            Text("0.00")
                        }
                    )

                    FilterChip(
                        selected = decimalPlaces == 3,
                        onClick = {
                            decimalPlaces = 3
                            scope.launch {
                                settingsManager.saveDecimalPlaces(3)
                            }
                        },
                        label = {
                            Text("0.000")
                        }
                    )

                    FilterChip(
                        selected = decimalPlaces == 4,
                        onClick = {
                            decimalPlaces = 4
                            scope.launch {
                                settingsManager.saveDecimalPlaces(4)
                            }
                        },
                        label = {
                            Text("0.0000")
                        }
                    )
                }
            }
        }

        Card(
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
                        onClick = {
                            viewModel.getRatesForCurrency(baseCurrency, RefreshSource.MANUAL_CARD)
                        },
                        enabled = !state.isManualLoading
                    ) {
                        if (state.isManualLoading) {
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

        Card(
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
                        checked = isAutoRefreshEnabled,
                        onCheckedChange = { isChecked ->
                            isAutoRefreshEnabled = isChecked
                            scope.launch {
                                settingsManager.saveAutoRefreshStatus(isChecked)
                            }
                        }
                    )
                }

                if (isAutoRefreshEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Frequency: every ${refreshInterval.roundToInt()} min")

                    Slider(
                        value = refreshInterval,
                        onValueChange = {
                            refreshInterval = it
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                settingsManager.saveRefreshInterval(
                                    refreshInterval.roundToInt().toFloat()
                                )
                            }
                        },
                        valueRange = 15f..60f,
                        steps = 8
                    )
                }
            }
        }
    }
}

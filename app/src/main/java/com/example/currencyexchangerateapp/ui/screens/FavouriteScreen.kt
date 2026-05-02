package com.example.currencyexchangerateapp.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.currencyexchangerateapp.viewmodel.MainViewModel
import com.example.currencyexchangerateapp.data.CurrencyData
import com.example.currencyexchangerateapp.data.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun FavouriteScreen(
    navController: NavController,
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    val scope = rememberCoroutineScope()
    val favouriteCurrencies by settingsManager.getFavourites().collectAsState(initial = emptySet())

    val baseCurrency by settingsManager.getBaseCurrency().collectAsState("PLN")
    val filteredCurrencies = CurrencyData.currencies.filter { it.code != baseCurrency }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "Manage Currencies",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(filteredCurrencies) { index, currency ->
                val isFavourite = favouriteCurrencies.contains(currency.code)

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = CurrencyData.getFlagUrl(currency.code),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = currency.code, fontWeight = FontWeight.Bold)
                                Text(
                                    text = currency.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Switch(
                            checked = isFavourite,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    val newFavourites = favouriteCurrencies.toMutableSet()
                                    if (checked) newFavourites.add(currency.code)
                                    else newFavourites.remove(currency.code)
                                    settingsManager.saveFavourites(newFavourites)
                                }
                            }
                        )
                    }
                }

                if (index < filteredCurrencies.lastIndex) {
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
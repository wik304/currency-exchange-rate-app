package com.example.currencyexchangerateapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
    val favouriteCurrencies by settingsManager.getFavourites().collectAsState(initial = null)

    val baseCurrency by settingsManager.getBaseCurrency().collectAsState("PLN")
    val filteredCurrencies = CurrencyData.currencies.filter { it.code != baseCurrency }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val screenWidthDp = configuration.screenWidthDp
    val columns = when {
        isLandscape && screenWidthDp >= 600 -> 3
        isLandscape -> 2
        screenWidthDp >= 600 -> 2
        else -> 1
    }

    if (favouriteCurrencies == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        if (!isLandscape) {
            Text(
                text = "Manage Currencies",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredCurrencies,
                key = { currency -> currency.code }
            ) { currency ->
                val isFavourite = favouriteCurrencies!!.contains(currency.code)

                FavouriteCurrencyCard(
                    currencyCode = currency.code,
                    currencyName = currency.name,
                    isFavourite = isFavourite,
                    onCheckedChange = { checked ->
                        scope.launch {
                            val newFavourites = favouriteCurrencies!!.toMutableSet()
                            if (checked) newFavourites.add(currency.code)
                            else newFavourites.remove(currency.code)
                            settingsManager.saveFavourites(newFavourites)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FavouriteCurrencyCard(
    currencyCode: String,
    currencyName: String,
    isFavourite: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    model = CurrencyData.getFlagUrl(currencyCode),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = currencyCode, fontWeight = FontWeight.Bold)
                    Text(
                        text = currencyName,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            key(currencyCode) {
                Switch(
                    checked = isFavourite,
                    onCheckedChange = { checked ->
                        onCheckedChange(checked)
                    }
                )
            }
        }
    }
}
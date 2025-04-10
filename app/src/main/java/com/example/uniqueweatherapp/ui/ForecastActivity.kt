package com.example.uniqueweatherapp.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uniqueweatherapp.model.ForecastItem
import com.example.uniqueweatherapp.viewmodel.ForecastViewModel

class ForecastActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val zipCode = intent.getStringExtra("ZIP_CODE") ?: ""

        setContent {
            MaterialTheme {
                val forecastViewModel: ForecastViewModel = viewModel()
                val forecast by forecastViewModel.forecast.observeAsState(emptyList())

                LaunchedEffect(Unit) {
                    Log.d("ForecastActivity", "Fetching forecast for ZIP: $zipCode")
                    forecastViewModel.fetchForecast(zipCode, "a1160aed969479de39cbe27819c9de63")
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    ForecastScreen(forecast = forecast)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(forecast: List<ForecastItem>) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("16-Day Forecast") },
                navigationIcon = {
                    IconButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(forecast) { item ->
                ForecastCard(item)
            }
        }
    }
}

@Composable
fun ForecastCard(item: ForecastItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.date, style = MaterialTheme.typography.titleMedium)
            Text(text = "Temp: ${item.temp}°C", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Humidity: ${item.humidity}%", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

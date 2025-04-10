package com.example.uniqueweatherapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.uniqueweatherapp.model.ForecastItem
import com.example.uniqueweatherapp.network.WeatherApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ForecastViewModel : ViewModel() {

    private val _forecast = MutableLiveData<List<ForecastItem>>()
    val forecast: LiveData<List<ForecastItem>> = _forecast

    fun fetchForecast(zipCode: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = WeatherApiClient.weatherApiService.getForecast(zipCode, apiKey)
                Log.d("ForecastViewModel", "API returned ${response.list.size} items")
                val items = response.list.map {
                    ForecastItem(
                        date = it.dt_txt,
                        temp = it.main.temp,
                        humidity = it.main.humidity
                    )
                }
                _forecast.value = items
            } catch (e: Exception) {
                _forecast.value = emptyList()
                Log.e("ForecastViewModel", "Error fetching forecast: ${e.message}", e)
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
}

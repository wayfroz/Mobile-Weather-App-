package com.example.uniqueweatherapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniqueweatherapp.model.ForecastItem
import com.example.uniqueweatherapp.network.WeatherApiClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ForecastViewModel : ViewModel() {

    private val _forecast = MutableLiveData<List<ForecastItem>>()
    val forecast: LiveData<List<ForecastItem>> = _forecast

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

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
                _errorMessage.value = when (e) {
                    is HttpException -> "Invalid ZIP code. Please try again."
                    else -> "Something went wrong: ${e.message}"
                }
                Log.e("ForecastViewModel", "Error fetching forecast: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

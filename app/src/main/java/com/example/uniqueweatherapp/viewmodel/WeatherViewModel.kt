package com.example.uniqueweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.uniqueweatherapp.model.WeatherResponse
import com.example.uniqueweatherapp.network.WeatherApiClient
import kotlinx.coroutines.launch
import android.util.Log

class WeatherViewModel : ViewModel() {

    private val _weather = MutableLiveData<WeatherResponse?>()
    val weather: LiveData<WeatherResponse?> = _weather

    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = WeatherApiClient.weatherApiService.getWeatherByCity(city, apiKey)
                _weather.value = response
                Log.d("WeatherViewModel", "Weather fetched successfully for $city")
            } catch (e: Exception) {
                _weather.value = null
                Log.e("WeatherViewModel", "Error fetching weather: ${e.message}", e)
            }
        }
    }
}

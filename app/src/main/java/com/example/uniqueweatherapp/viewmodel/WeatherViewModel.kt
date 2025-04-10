package com.example.uniqueweatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.uniqueweatherapp.model.WeatherResponse
import com.example.uniqueweatherapp.network.WeatherApiClient
import kotlinx.coroutines.launch
import android.util.Log
import retrofit2.HttpException

class WeatherViewModel : ViewModel() {

    private val _weather = MutableLiveData<WeatherResponse?>()
    val weather: LiveData<WeatherResponse?> = _weather
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = WeatherApiClient.weatherApiService.getCurrentWeather(city, apiKey)
                _weather.value = response
                Log.d("WeatherViewModel", "Weather fetched successfully for $city")
            } catch (e: Exception) {
                _weather.value = null
                _errorMessage.value = when (e) {
                    is HttpException -> "Invalid ZIP code. Please try again."
                    else -> "Something went wrong: ${e.message}"
                }
                Log.e("WeatherViewModel", "Error fetching weather: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

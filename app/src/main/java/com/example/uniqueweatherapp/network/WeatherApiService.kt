package com.example.uniqueweatherapp.network

import com.example.uniqueweatherapp.model.ForecastResponse
import com.example.uniqueweatherapp.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("zip") zip: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecast(
        @Query("zip") zip: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): ForecastResponse
}

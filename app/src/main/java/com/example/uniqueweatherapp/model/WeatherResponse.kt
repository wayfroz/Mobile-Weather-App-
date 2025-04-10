package com.example.uniqueweatherapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

@Serializable
data class Main(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    val humidity: Int,
    val pressure: Int
)

@Serializable
data class Weather(
    val description: String
)

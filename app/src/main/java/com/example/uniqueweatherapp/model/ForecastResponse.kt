package com.example.uniqueweatherapp.model
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val list: List<ForecastEntry>
)

@Serializable
data class ForecastEntry(
    val dt: Long,
    val main: ForecastMain,
    val weather: List<ForecastWeather>,
    val dt_txt: String
)

@Serializable
data class ForecastMain(
    val temp: Double,
    val humidity: Int
)

@Serializable
data class ForecastWeather(
    val main: String,
    val description: String
)

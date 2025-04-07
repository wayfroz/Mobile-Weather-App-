package com.example.uniqueweatherapp.network

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object WeatherApiClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    @OptIn(ExperimentalSerializationApi::class)
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(
            Json {
                ignoreUnknownKeys = true
            }.asConverterFactory("application/json".toMediaType())
        )
        .build()

    val weatherApiService: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}

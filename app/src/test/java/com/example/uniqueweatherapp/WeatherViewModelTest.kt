package com.example.uniqueweatherapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.uniqueweatherapp.model.Main
import com.example.uniqueweatherapp.model.Weather
import com.example.uniqueweatherapp.model.WeatherResponse
import com.example.uniqueweatherapp.network.WeatherApiClient
import io.mockk.coEvery
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class WeatherViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: WeatherViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = WeatherViewModel()

        mockkObject(WeatherApiClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchWeather success updates weather LiveData`() = runTest {
        // Arrange
        val fakeResponse = WeatherResponse(
            name = "Miami",
            main = Main(
                temp = 80.0,
                feelsLike = 82.0,
                humidity = 70,
                pressure = 1010
            ),
            weather = listOf(
                Weather(description = "Sunny")
            )
        )

        coEvery { WeatherApiClient.weatherApiService.getCurrentWeather(any(), any(), any()) } returns fakeResponse

        // Act
        viewModel.fetchWeather(city = "33101", apiKey = "fakeApiKey")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val weather = viewModel.weather.value
        assertNotNull(weather)
        assertEquals("Miami", weather?.name)
        assertEquals("Sunny", weather?.weather?.firstOrNull()?.description)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `fetchWeather failure updates errorMessage LiveData`() = runTest {
        // Arrange - simulate network failure
        coEvery { WeatherApiClient.weatherApiService.getCurrentWeather(any(), any(), any()) } throws Exception("Network error")

        // Act
        viewModel.fetchWeather(city = "99999", apiKey = "fakeApiKey")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertNull(viewModel.weather.value)
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value!!.contains("Something went wrong"))
    }

    @Test
    fun `clearError sets errorMessage to null`() {
        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.errorMessage.value)
    }
}

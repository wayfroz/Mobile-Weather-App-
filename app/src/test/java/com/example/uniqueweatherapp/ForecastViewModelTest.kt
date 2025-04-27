package com.example.uniqueweatherapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.uniqueweatherapp.model.*
import com.example.uniqueweatherapp.network.WeatherApiClient
import com.example.uniqueweatherapp.viewmodel.ForecastViewModel
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
class ForecastViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ForecastViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ForecastViewModel()

        mockkObject(WeatherApiClient)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchForecast success updates forecast LiveData`() = runTest {
        // Arrange
        val fakeForecastResponse = ForecastResponse(
            list = listOf(
                ForecastEntry(
                    dt = 123456789,
                    main = ForecastMain(temp = 75.0, humidity = 65),
                    weather = listOf(ForecastWeather(main = "Clouds", description = "Overcast")),
                    dt_txt = "2025-04-27 12:00:00"
                ),
                ForecastEntry(
                    dt = 987654321,
                    main = ForecastMain(temp = 77.0, humidity = 60),
                    weather = listOf(ForecastWeather(main = "Clear", description = "Clear sky")),
                    dt_txt = "2025-04-27 15:00:00"
                )
            )
        )

        coEvery { WeatherApiClient.weatherApiService.getForecast(any(), any(), any()) } returns fakeForecastResponse

        // Act
        viewModel.fetchForecast(zipCode = "33101", apiKey = "fakeApiKey")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val forecast = viewModel.forecast.value
        assertNotNull(forecast)
        assertEquals(2, forecast?.size)
        assertEquals("2025-04-27 12:00:00", forecast?.get(0)?.date)
        assertEquals(75.0, forecast?.get(0)?.temp)
        assertEquals(65, forecast?.get(0)?.humidity)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `fetchForecast failure updates errorMessage LiveData`() = runTest {
        // Arrange
        coEvery { WeatherApiClient.weatherApiService.getForecast(any(), any(), any()) } throws Exception("Network error")

        // Act
        viewModel.fetchForecast(zipCode = "99999", apiKey = "fakeApiKey")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.errorMessage.value!!.contains("Something went wrong"))
        assertTrue(viewModel.forecast.value?.isEmpty() == true)
    }

    @Test
    fun `clearError sets errorMessage to null`() {
        // Act
        viewModel.clearError()

        // Assert
        assertNull(viewModel.errorMessage.value)
    }
}

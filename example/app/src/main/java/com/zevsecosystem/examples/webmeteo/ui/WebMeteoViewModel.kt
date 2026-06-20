package com.zevsecosystem.examples.webmeteo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zevsecosystem.examples.webmeteo.data.WebMeteoLoadResult
import com.zevsecosystem.examples.webmeteo.data.WebMeteoRepository
import com.zevsecosystem.examples.webmeteo.model.WebMeteoUiState
import com.zevsecosystem.serversdk.webmeteo.WebMeteoForecastResponse
import com.zevsecosystem.serversdk.webmeteo.WebMeteoOutputType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WebMeteoViewModel(
    private val repository: WebMeteoRepository = WebMeteoRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<WebMeteoUiState>(WebMeteoUiState.Idle)
    val state: StateFlow<WebMeteoUiState> = _state.asStateFlow()

    fun load(
        token: String,
        latitudeText: String,
        longitudeText: String,
        heightText: String,
        type: WebMeteoOutputType,
        requestTimeText: String,
    ) {
        val latitude = latitudeText.trim().replace(',', '.').toDoubleOrNull()
        val longitude = longitudeText.trim().replace(',', '.').toDoubleOrNull()
        val height = heightText.trim().replace(',', '.').toDoubleOrNull()
        val requestTime = requestTimeText.trim().takeIf { it.isNotBlank() }?.toLongOrNull()

        if (latitude == null) {
            _state.value = WebMeteoUiState.Error("Введите широту числом.")
            return
        }

        if (longitude == null) {
            _state.value = WebMeteoUiState.Error("Введите долготу числом.")
            return
        }

        if (height == null) {
            _state.value = WebMeteoUiState.Error("Введите высоту числом.")
            return
        }

        if (requestTimeText.isNotBlank() && requestTime == null) {
            _state.value = WebMeteoUiState.Error("Unix timestamp должен быть числом.")
            return
        }

        viewModelScope.launch {
            _state.value = WebMeteoUiState.Loading

            repository.setToken(token)

            val result = repository.loadForecast(
                latitude = latitude,
                longitude = longitude,
                heightMeters = height,
                type = type,
                requestTimeUnixSeconds = requestTime,
            )

            _state.value = when (result) {
                is WebMeteoLoadResult.Success -> WebMeteoUiState.Success(
                    title = "Web Meteo ответ получен",
                    body = formatForecast(result.forecast),
                )

                is WebMeteoLoadResult.Error -> WebMeteoUiState.Error(result.message)
            }
        }
    }

    private fun formatForecast(forecast: WebMeteoForecastResponse): String {
        return buildString {
            appendLine("latitude: ${forecast.latitude}")
            appendLine("longitude: ${forecast.longitude}")
            appendLine("altitude: ${forecast.altitude}")
            appendLine("generationTimeMs: ${forecast.generationTimeMs}")
            appendLine()
            appendLine("current:")
            appendLine(forecast.current.toString())
            appendLine()
            appendLine("hourly:")
            appendLine(forecast.hourly.toString())
        }
    }
}


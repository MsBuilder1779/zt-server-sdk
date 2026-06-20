package com.zevsecosystem.examples.webmeteo.data

import com.zevsecosystem.serversdk.core.ZevsSdk
import com.zevsecosystem.serversdk.network.ZevsResult
import com.zevsecosystem.serversdk.webmeteo.WebMeteoForecastRequest
import com.zevsecosystem.serversdk.webmeteo.WebMeteoForecastResponse
import com.zevsecosystem.serversdk.webmeteo.WebMeteoOutputType
import kotlinx.serialization.json.JsonObject

class WebMeteoRepository {

    fun setToken(token: String?) {
        ZevsSdk.webMeteo.setToken(token?.trim()?.takeIf { it.isNotBlank() })
    }

    suspend fun loadForecast(
        latitude: Double,
        longitude: Double,
        heightMeters: Double,
        type: WebMeteoOutputType,
        requestTimeUnixSeconds: Long?,
    ): WebMeteoLoadResult {
        val validationError = validateInput(
            latitude = latitude,
            longitude = longitude,
            heightMeters = heightMeters,
        )

        if (validationError != null) {
            return WebMeteoLoadResult.Error(validationError)
        }

        val result = ZevsSdk.webMeteo.getForecast(
            lat = latitude,
            lon = longitude,
            height = heightMeters,
            requestTime = requestTimeUnixSeconds,
            type = type,
        )

        return when (result) {
            is ZevsResult.Success -> WebMeteoLoadResult.Success(result.data)
            is ZevsResult.Error -> WebMeteoLoadResult.Error(toUserMessage(result.error.toString()))
        }
    }

    suspend fun loadRawForecast(
        latitude: Double,
        longitude: Double,
        heightMeters: Double,
        type: WebMeteoOutputType,
        requestTimeUnixSeconds: Long?,
    ): ZevsResult<JsonObject> {
        val request = WebMeteoForecastRequest(
            lat = latitude,
            lon = longitude,
            height = heightMeters,
            requestTime = requestTimeUnixSeconds,
            type = type,
        )

        return ZevsSdk.webMeteo.getForecastRaw(request)
    }

    private fun validateInput(
        latitude: Double,
        longitude: Double,
        heightMeters: Double,
    ): String? {
        if (!latitude.isFinite() || latitude !in -90.0..90.0) {
            return "Некорректная широта. Значение должно быть от -90 до 90."
        }

        if (!longitude.isFinite() || longitude !in -180.0..180.0) {
            return "Некорректная долгота. Значение должно быть от -180 до 180."
        }

        if (!heightMeters.isFinite()) {
            return "Некорректная высота."
        }

        return null
    }

    private fun toUserMessage(error: String): String {
        return when {
            error.contains("token", ignoreCase = true) ||
            error.contains("WEB_METEO_TOKEN", ignoreCase = true) ->
                "Web Meteo token не задан или неверный."

            error.contains("unauthorized", ignoreCase = true) ||
            error.contains("forbidden", ignoreCase = true) ||
            error.contains("403", ignoreCase = true) ||
            error.contains("401", ignoreCase = true) ->
                "Нет доступа к Web Meteo."

            error.contains("timeout", ignoreCase = true) ||
            error.contains("network", ignoreCase = true) ||
            error.contains("connection", ignoreCase = true) ->
                "Ошибка сети. Проверьте интернет и повторите запрос."

            error.contains("parse", ignoreCase = true) ||
            error.contains("serialization", ignoreCase = true) ->
                "Ошибка обработки ответа Web Meteo."

            else ->
                "Не удалось получить метеоданные."
        }
    }
}

sealed class WebMeteoLoadResult {
    data class Success(val forecast: WebMeteoForecastResponse) : WebMeteoLoadResult()
    data class Error(val message: String) : WebMeteoLoadResult()
}


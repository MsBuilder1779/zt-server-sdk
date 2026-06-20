# Web Meteo API

Этот файл описывает только работу с **Web Meteo** через `ZT Server SDK`.

Общее подключение `.aar`, зависимости Gradle и базовая инициализация SDK описываются в основном `README.md`. Здесь — только конкретная функция Web Meteo и готовый код использования.

---

## Что делает Web Meteo

Web Meteo получает метеоданные по точке:

- широта;
- долгота;
- высота;
- тип результата;
- время прогноза, если нужно.

Web Meteo работает отдельно от обычной авторизации приложения. Для него используется отдельный `webMeteoToken`.

---

## Публичный API

Основная точка входа:

```kotlin
ZevsSdk.webMeteo
```

Основные методы:

```kotlin
ZevsSdk.webMeteo.getForecast(...)
ZevsSdk.webMeteo.getForecastRaw(...)
ZevsSdk.webMeteo.setToken(...)
```

---

## Типы результата

Для `type` используется `WebMeteoOutputType`:

```kotlin
WebMeteoOutputType.METEO
WebMeteoOutputType.METEO_FULL
WebMeteoOutputType.HEIGHT
WebMeteoOutputType.HEIGHT_FULL
```

Соответствие:

| SDK type | Назначение |
|---|---|
| `METEO` | обычный метео-ответ |
| `METEO_FULL` | расширенный метео-ответ |
| `HEIGHT` | данные по высоте |
| `HEIGHT_FULL` | расширенные данные по высоте |

---

## Вариант 1. Token передаётся при инициализации SDK

```kotlin
import com.zevsecosystem.serversdk.core.ZevsSdk
import com.zevsecosystem.serversdk.core.ZevsSdkConfig

ZevsSdk.init(
    context = applicationContext,
    config = ZevsSdkConfig(
        baseUrl = "https://zevs-team.ru",
        appVersionCode = BuildConfig.VERSION_CODE,
        appVersionName = BuildConfig.VERSION_NAME,
        webMeteoToken = "WEB_METEO_TOKEN"
    )
)
```

Используйте этот вариант, если Web Meteo token известен уже при старте приложения.

---

## Вариант 2. Token задаётся позже

```kotlin
ZevsSdk.webMeteo.setToken("WEB_METEO_TOKEN")
```

Используйте этот вариант, если token приходит позже: после входа пользователя, после загрузки настроек, после QR-кода, из защищённого хранилища и так далее.

Чтобы сбросить token:

```kotlin
ZevsSdk.webMeteo.setToken(null)
```

---

## Готовая реализация для приложения

Создайте файл в приложении:

```text
app/src/main/java/<your/package>/webmeteo/WebMeteoSdkRepository.kt
```

Код файла:

```kotlin
package your.package.webmeteo

import com.zevsecosystem.serversdk.core.ZevsSdk
import com.zevsecosystem.serversdk.network.ZevsResult
import com.zevsecosystem.serversdk.webmeteo.WebMeteoForecastRequest
import com.zevsecosystem.serversdk.webmeteo.WebMeteoForecastResponse
import com.zevsecosystem.serversdk.webmeteo.WebMeteoOutputType
import kotlinx.serialization.json.JsonObject

class WebMeteoSdkRepository {

    fun setWebMeteoToken(token: String?) {
        ZevsSdk.webMeteo.setToken(token)
    }

    suspend fun getMeteo(
        latitude: Double,
        longitude: Double,
        heightMeters: Double,
        requestTimeUnixSeconds: Long? = null,
        type: WebMeteoOutputType = WebMeteoOutputType.METEO,
    ): WebMeteoResult {
        if (!latitude.isFinite() || latitude !in -90.0..90.0) {
            return WebMeteoResult.Error("Некорректная широта")
        }

        if (!longitude.isFinite() || longitude !in -180.0..180.0) {
            return WebMeteoResult.Error("Некорректная долгота")
        }

        if (!heightMeters.isFinite()) {
            return WebMeteoResult.Error("Некорректная высота")
        }

        val result = ZevsSdk.webMeteo.getForecast(
            lat = latitude,
            lon = longitude,
            height = heightMeters,
            requestTime = requestTimeUnixSeconds,
            type = type,
        )

        return when (result) {
            is ZevsResult.Success -> WebMeteoResult.Success(result.data)
            is ZevsResult.Error -> WebMeteoResult.Error(mapWebMeteoError(result.error.toString()))
        }
    }

    suspend fun getMeteoRaw(
        latitude: Double,
        longitude: Double,
        heightMeters: Double,
        requestTimeUnixSeconds: Long? = null,
        type: WebMeteoOutputType = WebMeteoOutputType.METEO,
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

    private fun mapWebMeteoError(error: String): String {
        return when {
            error.contains("WEB_METEO_TOKEN_REQUIRED", ignoreCase = true) ->
                "Web Meteo token не задан"

            error.contains("UNAUTHORIZED", ignoreCase = true) ||
            error.contains("FORBIDDEN", ignoreCase = true) ->
                "Нет доступа к Web Meteo"

            error.contains("NETWORK", ignoreCase = true) ||
            error.contains("TIMEOUT", ignoreCase = true) ->
                "Ошибка сети при получении метеоданных"

            error.contains("PARSE", ignoreCase = true) ->
                "Ошибка обработки ответа Web Meteo"

            else ->
                "Не удалось получить метеоданные"
        }
    }
}

sealed class WebMeteoResult {
    data class Success(val forecast: WebMeteoForecastResponse) : WebMeteoResult()
    data class Error(val message: String) : WebMeteoResult()
}
```

---

## Пример вызова из ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zevsecosystem.serversdk.webmeteo.WebMeteoOutputType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import your.package.webmeteo.WebMeteoResult
import your.package.webmeteo.WebMeteoSdkRepository

class MeteoViewModel(
    private val webMeteoRepository: WebMeteoSdkRepository = WebMeteoSdkRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow<MeteoUiState>(MeteoUiState.Idle)
    val state: StateFlow<MeteoUiState> = _state

    fun setWebMeteoToken(token: String) {
        webMeteoRepository.setWebMeteoToken(token)
    }

    fun loadMeteo(
        latitude: Double,
        longitude: Double,
        heightMeters: Double,
        requestTimeUnixSeconds: Long? = null,
    ) {
        viewModelScope.launch {
            _state.value = MeteoUiState.Loading

            val result = webMeteoRepository.getMeteo(
                latitude = latitude,
                longitude = longitude,
                heightMeters = heightMeters,
                requestTimeUnixSeconds = requestTimeUnixSeconds,
                type = WebMeteoOutputType.METEO,
            )

            _state.value = when (result) {
                is WebMeteoResult.Success -> MeteoUiState.Success(result.forecast)
                is WebMeteoResult.Error -> MeteoUiState.Error(result.message)
            }
        }
    }
}
```

---

## UI state для примера

```kotlin
import com.zevsecosystem.serversdk.webmeteo.WebMeteoForecastResponse

sealed class MeteoUiState {
    data object Idle : MeteoUiState()
    data object Loading : MeteoUiState()
    data class Success(val forecast: WebMeteoForecastResponse) : MeteoUiState()
    data class Error(val message: String) : MeteoUiState()
}
```

---

## Пример чтения ответа

`WebMeteoForecastResponse` содержит основные поля ответа и карты данных:

```kotlin
val forecast = result.forecast

val latitude = forecast.latitude
val longitude = forecast.longitude
val altitude = forecast.altitude
val generationTimeMs = forecast.generationTimeMs

val current = forecast.current
val hourly = forecast.hourly
```

`current` и `hourly` — это карты значений, потому что набор метеополей может отличаться в зависимости от типа запроса и ответа сервера.

Пример безопасного чтения:

```kotlin
val temperatureList = forecast.current["temperature_2m"]
val firstTemperature = temperatureList?.firstOrNull()?.toString()
```

---

## Запрос на конкретное время

Если нужен прогноз на конкретное время, передайте Unix timestamp:

```kotlin
val result = webMeteoRepository.getMeteo(
    latitude = 55.75,
    longitude = 37.61,
    heightMeters = 180.0,
    requestTimeUnixSeconds = 1767225600L,
    type = WebMeteoOutputType.METEO,
)
```

Если время не нужно, передайте `null` или не указывайте параметр.

---

## Когда использовать getForecastRaw

Используйте `getForecastRaw`, если нужно получить сырой JSON-ответ без привязки к `WebMeteoForecastResponse`.

```kotlin
val rawResult = webMeteoRepository.getMeteoRaw(
    latitude = 55.75,
    longitude = 37.61,
    heightMeters = 180.0,
    type = WebMeteoOutputType.METEO_FULL,
)
```

Обычному приложению чаще нужен `getForecast`, а не `getForecastRaw`.

---

## Важные правила

1. Не собирайте URL Web Meteo вручную.
2. Не добавляйте Web Meteo token вручную в headers.
3. Не используйте `Authorization` для Web Meteo.
4. Не смешивайте Web Meteo token с обычными access / refresh токенами приложения.
5. Не логируйте Web Meteo token.
6. Не показывайте пользователю технический текст ошибки напрямую.
7. Проверяйте координаты и высоту до вызова SDK.
8. Для UI используйте свои понятные сообщения об ошибках.

---

## Минимальный рабочий сценарий

```kotlin
ZevsSdk.webMeteo.setToken("WEB_METEO_TOKEN")

val repository = WebMeteoSdkRepository()

val result = repository.getMeteo(
    latitude = 55.75,
    longitude = 37.61,
    heightMeters = 180.0,
    type = WebMeteoOutputType.METEO,
)

when (result) {
    is WebMeteoResult.Success -> {
        val forecast = result.forecast
        // показать forecast в UI
    }

    is WebMeteoResult.Error -> {
        val message = result.message
        // показать message в UI
    }
}
```

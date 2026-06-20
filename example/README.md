# ZT Web Meteo Example

Минимальное Android-приложение-пример для разработчиков, которые подключают **ZT Server SDK** и используют функцию **Web Meteo**.

Пример показывает полный простой сценарий:

- подключение SDK `.aar`;
- инициализацию SDK;
- ввод Web Meteo token;
- ввод координат и высоты;
- выбор типа Web Meteo ответа;
- выполнение запроса через SDK;
- обработку состояний `Loading / Success / Error`;
- вывод результата на экран.

---

## Состав проекта

```text
ZT-WebMeteo-Example/
├── README.md
├── settings.gradle.kts
├── build.gradle.kts
├── BUILD_CMD.bat
├── COPY_AAR_HERE.txt
└── app/
    ├── build.gradle.kts
    ├── libs/
    │   └── README_PUT_AAR_HERE.txt
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/zevsecosystem/examples/webmeteo/
            ├── WebMeteoExampleApp.kt
            ├── MainActivity.kt
            ├── data/
            │   └── WebMeteoRepository.kt
            ├── model/
            │   └── WebMeteoUiState.kt
            ├── ui/
            │   ├── WebMeteoScreen.kt
            │   └── WebMeteoViewModel.kt
            └── theme/
                └── WebMeteoExampleTheme.kt
```

---

## Что нужно сделать перед запуском

Перед сборкой нужно положить SDK AAR в приложение.

Скопируйте файл:

```text
zt-server-sdk-0.2.0.aar
```

в папку:

```text
app/libs/
```

Итоговый путь должен быть:

```text
app/libs/zt-server-sdk-0.2.0.aar
```

---

## Быстрый запуск через CMD

Если SDK лежит в локальном бинарном репозитории:

```text
D:\Projects\ZeVs\ZT - Server SDK\libs\zt-server-sdk-0.2.0.aar
```

а пример приложения лежит в:

```text
D:\Projects\ZeVs\ZT-WebMeteo-Example
```

выполните:

```bat
cd /d "D:\Projects\ZeVs\ZT-WebMeteo-Example"

copy /Y "D:\Projects\ZeVs\ZT - Server SDK\libs\zt-server-sdk-0.2.0.aar" "app\libs\zt-server-sdk-0.2.0.aar"

gradlew.bat assembleDebug
```

После успешной сборки откройте проект в Android Studio и запустите `app` на устройстве или эмуляторе.

---

## Подключение SDK в Gradle

В примере SDK подключается как локальный `.aar` файл.

Файл:

```text
app/libs/zt-server-sdk-0.2.0.aar
```

Зависимость в `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/zt-server-sdk-0.2.0.aar"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

---

## Разрешение Internet

В `AndroidManifest.xml` уже добавлено разрешение:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Без этого приложение не сможет выполнить Web Meteo запрос.

---

## Инициализация SDK

SDK инициализируется один раз при запуске приложения.

Файл:

```text
app/src/main/java/com/zevsecosystem/examples/webmeteo/WebMeteoExampleApp.kt
```

Код:

```kotlin
package com.zevsecosystem.examples.webmeteo

import android.app.Application
import com.zevsecosystem.serversdk.ZevsSdk
import com.zevsecosystem.serversdk.core.ZevsSdkConfig

class WebMeteoExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        ZevsSdk.init(
            context = applicationContext,
            config = ZevsSdkConfig(
                baseUrl = "https://zevs-team.ru",
                appVersionCode = BuildConfig.VERSION_CODE,
                appVersionName = BuildConfig.VERSION_NAME,
            )
        )
    }
}
```

---

## Главный файл интеграции с Web Meteo

Вся работа с Web Meteo вынесена в один файл:

```text
app/src/main/java/com/zevsecosystem/examples/webmeteo/data/WebMeteoRepository.kt
```

Именно этот файл разработчик обычно переносит или адаптирует под своё приложение.

Он делает:

- установку Web Meteo token;
- проверку широты;
- проверку долготы;
- проверку высоты;
- вызов `ZevsSdk.webMeteo.getForecast(...)`;
- преобразование ошибок SDK в понятные сообщения для UI.

---

## Основной вызов Web Meteo

Пример использует такой вызов SDK:

```kotlin
val result = ZevsSdk.webMeteo.getForecast(
    lat = latitude,
    lon = longitude,
    height = heightMeters,
    requestTime = requestTimeUnixSeconds,
    type = type,
)
```

Где:

```text
lat         — широта
lon         — долгота
height      — высота точки в метрах
requestTime — Unix timestamp, если нужен прогноз на конкретное время
type        — тип Web Meteo ответа
```

---

## Web Meteo token

Token задаётся перед запросом:

```kotlin
ZevsSdk.webMeteo.setToken(token)
```

В примере token вводится в UI вручную.

В реальном приложении token можно брать из:

- настроек приложения;
- защищённого хранилища;
- профиля пользователя;
- QR-кода;
- другого безопасного источника.

Token не нужно вручную добавлять в headers.

---

## Типы ответа

В примере используется `WebMeteoOutputType`.

Доступные варианты:

```kotlin
WebMeteoOutputType.METEO
WebMeteoOutputType.METEO_FULL
WebMeteoOutputType.HEIGHT
WebMeteoOutputType.HEIGHT_FULL
```

В UI они отображаются как переключатели.

---

## Экран приложения

Файл экрана:

```text
app/src/main/java/com/zevsecosystem/examples/webmeteo/ui/WebMeteoScreen.kt
```

На экране есть:

- поле Web Meteo token;
- поле широты;
- поле долготы;
- поле высоты;
- поле Unix timestamp;
- выбор типа ответа;
- кнопка запроса;
- блок результата.

---

## ViewModel

Файл:

```text
app/src/main/java/com/zevsecosystem/examples/webmeteo/ui/WebMeteoViewModel.kt
```

ViewModel отвечает за:

- чтение строк из UI;
- преобразование строк в числа;
- проверку пустых и некорректных значений;
- запуск coroutine;
- вызов repository;
- публикацию состояния UI.

Состояния UI:

```kotlin
sealed class WebMeteoUiState {
    data object Idle : WebMeteoUiState()
    data object Loading : WebMeteoUiState()
    data class Success(
        val title: String,
        val body: String,
    ) : WebMeteoUiState()
    data class Error(
        val message: String,
    ) : WebMeteoUiState()
}
```

---

## Пример входных данных

Можно проверить запрос на таких данных:

```text
latitude: 55.75
longitude: 37.61
height: 180
type: METEO
requestTime: пусто
```

Если нужен прогноз на конкретное время, введите Unix timestamp в секундах.

---

## Что разработчику нужно заменить

В своём приложении разработчику обычно нужно заменить:

```text
package com.zevsecosystem.examples.webmeteo
```

на свой package.

Также нужно решить, откуда брать Web Meteo token:

```kotlin
repository.setToken(token)
```

В примере token вводится вручную, но в реальном приложении его лучше не хранить в открытом виде и не выводить в лог.

---

## Что не нужно делать

Приложение не должно вручную:

- собирать внутренний серверный URL;
- добавлять служебные SDK headers;
- передавать идентификатор устройства;
- передавать ключ приложения;
- использовать Web Meteo token как обычный access token;
- логировать token;
- показывать пользователю внутренний текст ошибки.

SDK сам обрабатывает служебную часть.

---

## Возможные ошибки

Пример показывает пользователю понятные сообщения:

```text
Введите широту числом.
Введите долготу числом.
Введите высоту числом.
Unix timestamp должен быть числом.
Web Meteo token не задан или неверный.
Нет доступа к Web Meteo.
Ошибка сети. Проверьте интернет и повторите запрос.
Ошибка обработки ответа Web Meteo.
Не удалось получить метеоданные.
```

---

## Сборка

CMD:

```bat
cd /d "D:\Projects\ZeVs\ZT-WebMeteo-Example"
gradlew.bat assembleDebug
```

PowerShell:

```powershell
cd "D:\Projects\ZeVs\ZT-WebMeteo-Example"
./gradlew assembleDebug
```

---

## Проверка перед запуском

Перед запуском проверьте:

- файл `app/libs/zt-server-sdk-0.2.0.aar` существует;
- проект открывается в Android Studio;
- интернет-разрешение добавлено;
- SDK инициализируется в `WebMeteoExampleApp`;
- token введён в UI;
- координаты и высота заданы корректно.

---

## Минимальная логика работы

```text
UI
 ↓
WebMeteoViewModel
 ↓
WebMeteoRepository
 ↓
ZevsSdk.webMeteo.getForecast(...)
 ↓
WebMeteoUiState.Success / WebMeteoUiState.Error
 ↓
UI
```

---

## Назначение проекта

Этот проект не является полноценным приложением.

Это минимальный пример для разработчиков, чтобы быстро понять, как подключить SDK и вызвать Web Meteo без раскрытия внутренней реализации SDK.

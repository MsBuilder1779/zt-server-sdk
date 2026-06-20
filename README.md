# ZT Server SDK

Готовый Android SDK для подключения приложений ZT / ZEvS к серверной экосистеме ZEvS.

Репозиторий содержит только готовый бинарный файл SDK:

```text
libs/zt-server-sdk-0.2.0.aar
```

Исходный код SDK в этом репозитории не публикуется.

## Версия

```text
0.2.0
```

## Что делает SDK

ZT Server SDK даёт Android-приложению готовый слой для работы с сервером ZEvS.

Основные возможности:

- инициализация SDK в Android-приложении;
- регистрация и авторизация приложения на сервере;
- выполнение защищённых запросов к серверу;
- автоматическая работа с access / refresh токенами;
- получение информации о пользователе / профиле;
- получение статуса сервера;
- получение команд от сервера;
- отправка результата выполнения команд;
- поддержка Web Meteo запросов;
- единая обработка сетевых ошибок и ответов сервера.

SDK сам управляет служебными данными авторизации и защищёнными заголовками. Приложению не нужно вручную передавать токены, идентификаторы устройства или служебные ключи.

## Подключение SDK

Скопируйте файл SDK в Android-приложение:

```text
app/libs/zt-server-sdk-0.2.0.aar
```

Добавьте зависимость в `app/build.gradle`:

```gradle
dependencies {
    implementation files("libs/zt-server-sdk-0.2.0.aar")

    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3"
    implementation "com.squareup.okhttp3:okhttp:4.12.0"
}
```

## Разрешение Internet

Добавьте разрешение в `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Инициализация SDK

Инициализируйте SDK один раз при старте приложения, например в `Application` или в точке запуска приложения:

```kotlin
import com.zevsecosystem.serversdk.ZevsSdk
import com.zevsecosystem.serversdk.core.ZevsSdkConfig

ZevsSdk.init(
    context = applicationContext,
    config = ZevsSdkConfig(
        baseUrl = "https://zevs-team.ru",
        appVersionCode = BuildConfig.VERSION_CODE,
        appVersionName = BuildConfig.VERSION_NAME
    )
)
```

## Правила использования

Приложение не должно вручную передавать:

- `device_pk`;
- `app_key` / `apk_key`;
- access token;
- refresh token;
- `Authorization`;
- `X-Device-PK`;
- `X-App-Key`;
- `X-Bootstrap-Secret`.

Эти данные SDK обрабатывает самостоятельно.

## Web Meteo

Если приложение использует Web Meteo, передавайте только Web Meteo token через предназначенный для этого публичный API / конфигурацию SDK.

Не смешивайте Web Meteo token с обычной авторизацией приложения.

## Безопасность

Этот репозиторий содержит только скомпилированный AAR-файл SDK.

Проверка доступа, права пользователя, привязка устройства, проверка токенов и основные ограничения выполняются на стороне сервера.

## Файл SDK

Текущий файл SDK:

```text
libs/zt-server-sdk-0.2.0.aar
```

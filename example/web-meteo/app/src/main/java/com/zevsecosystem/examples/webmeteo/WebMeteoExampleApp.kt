package com.zevsecosystem.examples.webmeteo

import android.app.Application
import com.zevsecosystem.serversdk.core.ZevsSdk
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


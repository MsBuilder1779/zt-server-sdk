package com.zevsecosystem.examples.webmeteo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zevsecosystem.examples.webmeteo.theme.WebMeteoExampleTheme
import com.zevsecosystem.examples.webmeteo.ui.WebMeteoScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WebMeteoExampleTheme {
                WebMeteoScreen()
            }
        }
    }
}


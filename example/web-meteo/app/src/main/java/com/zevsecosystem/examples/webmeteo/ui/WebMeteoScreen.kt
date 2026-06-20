package com.zevsecosystem.examples.webmeteo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zevsecosystem.examples.webmeteo.model.WebMeteoUiState
import com.zevsecosystem.serversdk.webmeteo.WebMeteoOutputType

@Composable
fun WebMeteoScreen(
    vm: WebMeteoViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()

    var token by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("55.75") }
    var longitude by remember { mutableStateOf("37.61") }
    var height by remember { mutableStateOf("180") }
    var requestTime by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(WebMeteoOutputType.METEO) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "ZT Web Meteo Example",
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = "Пример Android-приложения для вызова Web Meteo через ZT Server SDK.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Web Meteo token") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Широта") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Долгота") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Высота, м") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = requestTime,
                        onValueChange = { requestTime = it },
                        label = { Text("Unix timestamp, опционально") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Тип ответа",
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WebMeteoOutputType.entries.forEach { item ->
                            FilterChip(
                                selected = item == type,
                                onClick = { type = item },
                                label = { Text(item.name) },
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                latitude = "55.75"
                                longitude = "37.61"
                                height = "180"
                                requestTime = ""
                                type = WebMeteoOutputType.METEO
                            }
                        ) {
                            Text("Пример")
                        }

                        Button(
                            onClick = {
                                vm.load(
                                    token = token,
                                    latitudeText = latitude,
                                    longitudeText = longitude,
                                    heightText = height,
                                    type = type,
                                    requestTimeText = requestTime,
                                )
                            }
                        ) {
                            Text("Получить метео")
                        }
                    }
                }
            }

            ResultBlock(state)
        }
    }
}

@Composable
private fun ResultBlock(state: WebMeteoUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Результат",
                style = MaterialTheme.typography.titleMedium,
            )

            when (state) {
                WebMeteoUiState.Idle -> {
                    Text("Запрос ещё не выполнялся.")
                }

                WebMeteoUiState.Loading -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator()
                        Text("Получение метеоданных...")
                    }
                }

                is WebMeteoUiState.Success -> {
                    Text(
                        text = state.title,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = state.body,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                is WebMeteoUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}


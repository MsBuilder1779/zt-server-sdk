package com.zevsecosystem.examples.webmeteo.model

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


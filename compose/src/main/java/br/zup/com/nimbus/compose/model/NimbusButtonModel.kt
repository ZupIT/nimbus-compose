package br.zup.com.nimbus.compose.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NimbusButtonModel(
    val text: String,
    val onPress: () -> Unit,
)

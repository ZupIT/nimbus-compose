package br.com.zup.nimbus.compose.sample.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NimbusButtonModel(
    val text: String,
    val onPress: () -> Unit,
)

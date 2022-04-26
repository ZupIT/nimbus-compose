package br.zup.com.nimbus.compose.sample.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BeagleButtonModel(
    val text: String,
    val onPress: () -> Unit,
)

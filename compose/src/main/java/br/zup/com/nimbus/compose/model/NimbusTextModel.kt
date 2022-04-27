package br.zup.com.nimbus.compose.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NimbusTextModel (
    val text: String,
)

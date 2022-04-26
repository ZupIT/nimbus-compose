package br.zup.com.nimbus.compose.sample.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class PersonCardModel(
    val person: Person,
    val address: Address,
)
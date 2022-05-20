package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.compose.sample.model.PersonCardModel
import br.zup.com.nimbus.compose.model.NimbusTextModel
import br.zup.com.nimbus.compose.core.ui.parse
import br.zup.com.nimbus.compose.ComponentHandler
import com.fasterxml.jackson.core.type.TypeReference

val customComponents: Map<String, @Composable ComponentHandler> = mapOf(
    "custom:text" to @Composable { element, _ ->
        CustomText(element.parse(object : TypeReference<NimbusTextModel>() {})) },
    "custom:personCard" to @Composable { element, _ ->
        PersonCardComponent(element.parse(object : TypeReference<PersonCardModel>() {})) },
)

package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.compose.sample.model.PersonCardModel
import br.zup.com.nimbus.compose.model.NimbusTextModel
import br.zup.com.nimbus.compose.ComponentHandler
import com.fasterxml.jackson.core.type.TypeReference

val customComponents: Map<String, @Composable ComponentHandler> = mapOf(
    "custom:text" to @Composable { element, _ ->
        CustomText(element.parse(object : TypeReference<NimbusTextModel>() {})) },
    "custom:personCard" to @Composable { element, _ ->
        PersonCardComponent(element.parse(object : TypeReference<PersonCardModel>() {})) },
    "material:text" to @Composable { element, _ ->
        NimbusText(element.parse(object : TypeReference<NimbusTextModel>() {}))
    },
    "layout:container" to @Composable { _, children -> NimbusContainer(children) },
    "material:button" to @Composable { element, _ ->
        // can't use jackson to deserialize this, it has a function.
        NimbusButton(
            text = element.properties?.get("text") as String,
            onPress = element.properties!!["onPress"] as (Any?) -> Unit,
        )
    },
)

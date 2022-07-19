package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.compose.sample.model.PersonCardModel
import br.com.zup.nimbus.compose.sample.model.NimbusTextModel
import br.zup.com.nimbus.compose.ComponentHandler
import br.zup.com.nimbus.compose.ComponentLibrary
import com.fasterxml.jackson.core.type.TypeReference

val layoutLib = ComponentLibrary("layout")
    .add("container") @Composable { NimbusContainer(it.children) }
    //.add("text") @Composable { NormalText(it) }

/*val customComponents: Map<String, @Composable ComponentHandler> = mapOf(
    "custom:text" to @Composable { element, _ , _->
        CustomText(element.parse(object : TypeReference<NimbusTextModel>() {})) },45
    "custom:personCard" to @Composable { element, _ , _->
        PersonCardComponent(element.parse(object : TypeReference<PersonCardModel>() {})) },
    "material:text" to @Composable { element, _ , _->
        NimbusText(element.parse(object : TypeReference<NimbusTextModel>() {}))
    },
    "layout:container" to @Composable { _, children, _ -> NimbusContainer(children) },
    "material:button" to @Composable { element, _ , _->
        // can't use jackson to deserialize this, it has a function.
        NimbusButton(
            text = element.properties?.get("text") as String,
            onPress = element.properties!!["onPress"] as (Any?) -> Unit,
        )
    },
    "custom:text" to @Composable { _, _, _ ->
        NormalText(text = "", size = 1, isBold = true, onChange = {})
    }
)*/

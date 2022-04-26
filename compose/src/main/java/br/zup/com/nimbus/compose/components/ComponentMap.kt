package br.zup.com.nimbus.compose.components

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.model.NimbusTextModel
import br.zup.com.nimbus.compose.parse
import com.fasterxml.jackson.core.type.TypeReference
import com.zup.nimbus.core.tree.ServerDrivenNode

val components: Map<String, @Composable (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit> =
    mapOf(
        "material:text" to @Composable { element, _ ->
            NimbusText(element.parse(object : TypeReference<NimbusTextModel>() {}))
        },
        "layout:container" to @Composable { _, children -> NimbusContainer(children) },
        "material:button" to @Composable { element, _ ->
            // can't use jackson to deserialize this, it has a function.
            NimbusButton(
                text = element.properties?.get("text") as String,
                onPress = element.properties!!["onPress"] as () -> Unit,
            )
        },
    )
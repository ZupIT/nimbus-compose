package br.zup.com.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.model.BeagleTextModel
import br.zup.com.nimbus.compose.sample.model.PersonCardModel
import br.zup.com.nimbus.compose.parse
import com.fasterxml.jackson.core.type.TypeReference
import com.zup.nimbus.core.tree.ServerDrivenNode

val customComponents: Map<String, @Composable (element: ServerDrivenNode, children: @Composable () -> Unit) -> Unit> = mapOf(
    "custom:text" to @Composable { element, _ -> CustomText(element.parse(object : TypeReference<BeagleTextModel>() {})) },
    "custom:personCard" to @Composable { element, _ -> PersonCardComponent(element.parse(object : TypeReference<PersonCardModel>() {})) },
)
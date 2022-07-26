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

package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.ComponentLibrary

val layoutLib = ComponentLibrary("layout")
    .add("container") @Composable { NimbusContainer(it.children) }

val customLib = ComponentLibrary("custom")
    .add("text") @Composable { NimbusText(it) }
    .add("textInput") @Composable { TextInput(it) }

val materialLib = ComponentLibrary("material")
    .add("text") @Composable { NimbusText(it) }
    .add("button") @Composable { NimbusButton(it) }
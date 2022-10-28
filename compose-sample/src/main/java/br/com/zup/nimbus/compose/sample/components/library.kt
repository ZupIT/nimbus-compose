package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.ui.NimbusComposeUILibrary

val layoutLib = NimbusComposeUILibrary("layout")
    .addComponent("container") @Composable { NimbusContainer(it) }

val customLib = NimbusComposeUILibrary("custom")
    .addComponent("text") @Composable { NimbusText(it) }
    .addComponent("textInput") @Composable { TextInput(it) }

val materialLib = NimbusComposeUILibrary("material")
    .addComponent("text") @Composable { NimbusText(it) }
    .addComponent("button") @Composable { NimbusButton(it) }

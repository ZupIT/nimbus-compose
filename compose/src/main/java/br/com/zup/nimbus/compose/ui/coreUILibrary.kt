package br.com.zup.nimbus.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import br.com.zup.nimbus.core.ui.coreUILibrary

val composeUILibrary = NimbusComposeUILibrary()
    .addComponent("fragment") @Composable {
        Column { it.children() }
    }
    // todo: add action "openUrl"
    .merge(coreUILibrary)

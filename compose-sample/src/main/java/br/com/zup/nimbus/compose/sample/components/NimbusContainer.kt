package br.com.zup.nimbus.compose.sample.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable

@Composable
fun NimbusContainer(children: @Composable () -> Unit) {
    Column() {
        children()
    }
}

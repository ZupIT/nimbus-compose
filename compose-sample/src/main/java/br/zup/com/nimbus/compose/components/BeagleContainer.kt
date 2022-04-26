package br.zup.com.nimbus.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable

@Composable
fun BeagleContainer(children: @Composable () -> Unit) {
    Column() {
        children()
    }
}
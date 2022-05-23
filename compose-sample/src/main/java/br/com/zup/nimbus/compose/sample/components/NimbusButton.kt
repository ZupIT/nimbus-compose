package br.zup.com.nimbus.compose.core.ui.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun NimbusButton(text: String, onPress: (Any?) -> Unit) {
    Button(content = { Text(text) }, onClick = { onPress(null) })
}

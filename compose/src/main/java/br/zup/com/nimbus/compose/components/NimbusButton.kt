package br.zup.com.nimbus.compose.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun NimbusButton(text: String, onPress: () -> Unit) {
    Button(content = { Text(text) }, onClick = onPress)
}

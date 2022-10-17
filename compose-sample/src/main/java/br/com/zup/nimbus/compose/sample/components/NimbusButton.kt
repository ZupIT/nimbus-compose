package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.zup.nimbus.processor.annotation.AutoDeserialize

@Composable
//@AutoDeserialize
fun NimbusButton(text: String, onPress: () -> Unit) {
    Button(content = { Text(text) }, onClick = { onPress() })
}

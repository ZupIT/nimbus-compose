package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.com.zup.nimbus.compose.sample.model.Box
import br.com.zup.nimbus.annotation.AutoDeserialize
import br.com.zup.nimbus.annotation.Root

@Composable
@AutoDeserialize
fun NimbusButton(text: String, @Root style: Box?, onPress: () -> Unit) {
    Button(content = { Text(text) }, onClick = { onPress() })
}

package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.com.zup.nimbus.annotation.AutoDeserialize

@Composable
@AutoDeserialize
fun NimbusText(text: String) {
    Text(text = text)
}

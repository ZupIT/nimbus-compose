package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.zup.nimbus.processor.ServerDrivenComponent

@Composable
@ServerDrivenComponent
fun NimbusText(text: String) {
    Text(text = text)
}

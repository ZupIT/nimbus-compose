package br.zup.com.nimbus.compose.core.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.model.NimbusTextModel

@Composable
fun NimbusText(props: NimbusTextModel) {
    Text(text = props.text)
}

package br.zup.com.nimbus.compose.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.model.BeagleTextModel

@Composable
fun BeagleText(props: BeagleTextModel) {
    Text(text = props.text)
}
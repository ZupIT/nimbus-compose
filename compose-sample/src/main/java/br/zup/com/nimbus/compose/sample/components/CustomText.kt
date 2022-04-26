package br.zup.com.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.model.BeagleTextModel

@Composable
fun CustomText(props: BeagleTextModel) {
    Text(text = props.text)
}
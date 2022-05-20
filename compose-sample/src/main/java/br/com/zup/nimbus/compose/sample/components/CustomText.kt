
package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.model.NimbusTextModel

@Composable
fun CustomText(props: NimbusTextModel) {
    Text(text = props.text)
}

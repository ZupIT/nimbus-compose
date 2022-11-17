package br.com.zup.nimbus.compose.sample.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.zup.nimbus.annotation.AutoDeserialize
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.NimbusTheme
import com.zup.nimbus.core.deserialization.AnyServerDrivenData

private fun getColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor(colorString))
}

@Composable
@AutoDeserialize
fun NimbusContainer(
    backgroundColor: String?,
    padding: Double?,
    content: @Composable () -> Unit,
) {
    var modifier = Modifier.padding((padding ?: 0.0).dp)
    backgroundColor?.let { modifier = modifier.background(color = getColor(it)) }
    Column(modifier = modifier) { content() }
}

package br.com.zup.nimbus.compose.sample.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.NimbusTheme
import com.zup.nimbus.core.deserialization.ComponentDeserializer

private fun getColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor(colorString))
}

@Composable
fun NimbusContainer(it: ComponentData) {
    val logger = NimbusTheme.nimbus.logger
    val deserializer = ComponentDeserializer(logger, it.node)
    deserializer.start()
    val background = deserializer.asStringOrNull("backgroundColor")
    val padding = deserializer.asDoubleOrNull("padding")
    var modifier = Modifier.padding((padding ?: 0.0).dp)
    if (background != null) modifier = modifier.background(color = getColor(background))
    Column(modifier = modifier) {
        it.children()
    }
}

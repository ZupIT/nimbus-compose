package br.com.zup.nimbus.compose.sample.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import br.com.zup.nimbus.compose.sample.BuildConfig
import br.zup.com.nimbus.compose.ComponentData
import br.zup.com.nimbus.compose.NimbusTheme
import com.zup.nimbus.core.deserialization.ComponentDeserializer


/*
data class NimbusLayoutModifier(
    val width: Double? = null,
    val height: Double? = null,
    val flex: String? = null,
)

object ServerDrivenEntityDeserializer {
    fun nimbusLayoutModifier(properties: ComponentDeserializer) : NimbusLayoutModifier {
        val width = properties.asDoubleOrNull("width")
        val height = properties.asDoubleOrNull("height")
        val flex = properties.asStringOrNull("flex")
        return NimbusLayoutModifier(width = width, height = height, flex = flex)
    }
}*/

enum class MyFontWeight {
    Normal,
    Bold,
    Bolder,
}

@Composable
fun NormalText(
    text: String,
    size: Int?,
    fontWeight: MyFontWeight?,
) {
    Text(
        text,
        fontSize = size?.sp ?: TextUnit.Unspecified,
        fontWeight = if (fontWeight == null) FontWeight.Normal else FontWeight.Bold,
    )
}

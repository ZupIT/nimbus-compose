package androidx.compose.material

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val printedByTextComponent = mutableListOf<Pair<String, Color>>()

@Composable
fun Text(text: String, color: Color = Color.Black) {
    printedByTextComponent.add(text to color)
}

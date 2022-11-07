package androidx.compose.foundation.layout

import androidx.compose.runtime.Composable

@Composable
fun Column(content: @Composable () -> Unit) {
    content()
}

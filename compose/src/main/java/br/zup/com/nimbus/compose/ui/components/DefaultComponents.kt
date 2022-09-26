package br.zup.com.nimbus.compose.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun LoadingDefault(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}

@Composable
internal fun ErrorDefault(throwable: Throwable, retry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Error ${throwable.message}")
        Button(onClick = retry) {
            Text("Retry")
        }
    }
}

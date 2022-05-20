package br.com.zup.nimbus.compose.sample.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomError(throwable: Throwable, retry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("Unexpected error: ${throwable.message}")
        Button(onClick = retry) {
            Text("Try Again!")
        }
    }
}
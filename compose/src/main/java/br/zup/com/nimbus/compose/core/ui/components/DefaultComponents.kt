package br.zup.com.nimbus.compose.core.ui.components

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LoadingDefault(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier)
}

@Composable
fun ErrorDefault(throwable: Throwable, modifier: Modifier = Modifier) {
    Text("Error ${throwable.message}", modifier = modifier)
}
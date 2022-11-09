package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.annotation.AutoDeserialize

object MyTest {
    @AutoDeserialize
    @Composable
    fun ComponentA(value: String) {}

    @AutoDeserialize
    @Composable
    fun ComponentB(value: String) {}
}

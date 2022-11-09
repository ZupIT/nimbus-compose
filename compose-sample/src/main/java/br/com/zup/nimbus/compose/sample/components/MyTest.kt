package br.com.zup.nimbus.compose.sample.components

import androidx.compose.runtime.Composable
import br.com.zup.nimbus.annotation.AutoDeserialize

class MyTest {
    companion object {
        @AutoDeserialize
        @Composable
        fun ComponentA(value: String) {}

        @AutoDeserialize
        @Composable
        fun ComponentB(value: String) {}
    }

    fun test(): String {
        return "test"
    }
}

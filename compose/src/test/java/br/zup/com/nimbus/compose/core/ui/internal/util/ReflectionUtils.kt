package br.zup.com.nimbus.compose.core.ui.internal.util

internal fun Any.invokeHiddenMethod(name: String) {
    val method = this.javaClass.getDeclaredMethod(name)
    method.isAccessible = true
    method.invoke(this)
}
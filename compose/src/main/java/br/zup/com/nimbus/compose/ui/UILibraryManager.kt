package br.zup.com.nimbus.compose.ui

import br.zup.com.nimbus.compose.ComponentHandler
import com.zup.nimbus.core.ui.UILibraryManager

fun UILibraryManager.getComponent(identifier: String): ComponentHandler? {
    val (namespace, name) = UILibraryManager.splitIdentifier(identifier) ?: return null
    val lib = getLibrary(namespace)
    return if (lib is NimbusComposeUILibrary) lib.getComponent(name) else null
}

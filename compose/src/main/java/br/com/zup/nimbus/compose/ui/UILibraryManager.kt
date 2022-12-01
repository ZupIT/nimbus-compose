package br.com.zup.nimbus.compose.ui

import br.com.zup.nimbus.core.ui.UILibraryManager
import br.com.zup.nimbus.compose.ComponentHandler

fun UILibraryManager.getComponent(identifier: String): ComponentHandler? {
    val (namespace, name) = UILibraryManager.splitIdentifier(identifier) ?: return null
    val lib = getLibrary(namespace)
    return if (lib is NimbusComposeUILibrary) lib.getComponent(name) else null
}

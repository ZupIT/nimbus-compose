package br.zup.com.nimbus.compose.ui

import br.com.zup.nimbus.core.ActionHandler
import br.com.zup.nimbus.core.ActionInitializationHandler
import br.com.zup.nimbus.core.OperationHandler
import br.com.zup.nimbus.core.ui.UILibrary
import br.zup.com.nimbus.compose.ComponentHandler

class NimbusComposeUILibrary(namespace: String = ""): UILibrary(namespace) {
    private val components = HashMap<String, ComponentHandler>()

    fun addComponent(name: String, handler: ComponentHandler): NimbusComposeUILibrary {
        components[name] = handler
        return this
    }

    fun getComponent(name: String): ComponentHandler? {
        return components[name]
    }

    override fun addAction(name: String, handler: ActionHandler): NimbusComposeUILibrary {
        super.addAction(name, handler)
        return this
    }

    override fun addActionInitializer(
        name: String,
        handler: ActionInitializationHandler,
    ): NimbusComposeUILibrary {
        super.addActionInitializer(name, handler)
        return this
    }

    override fun addActionObserver(observer: ActionHandler): NimbusComposeUILibrary {
        super.addActionObserver(observer)
        return this
    }

    override fun addOperation(name: String, handler: OperationHandler): NimbusComposeUILibrary {
        super.addOperation(name, handler)
        return this
    }

    fun merge(other: NimbusComposeUILibrary): NimbusComposeUILibrary {
        super.merge(other)
        components.putAll(other.components)
        return this
    }
}

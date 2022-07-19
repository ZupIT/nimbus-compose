package br.zup.com.nimbus.compose

class ComponentLibrary(private val namespace: String) {
    val components = HashMap<String, ComponentHandler>()

    fun add(name: String, handler: ComponentHandler): ComponentLibrary {
        components["$namespace:$name"] = handler
        return this
    }
}

package test.utils

import br.com.zup.nimbus.processor.NimbusCompose
import com.zup.nimbus.core.ActionTriggeredEvent
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.ServerDrivenState
import com.zup.nimbus.core.ServerDrivenView
import com.zup.nimbus.core.dependency.Dependent
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.scope.Scope
import com.zup.nimbus.core.tree.ServerDrivenAction
import com.zup.nimbus.core.tree.ServerDrivenEvent
import com.zup.nimbus.core.tree.ServerDrivenNode

const val DEFAULT_COMPONENT_NAME = "test:component"
const val DEFAULT_COMPONENT_ID = "test"
const val DEFAULT_ACTION_NAME = "test:action"
const val DEFAULT_EVENT_NAME = "onPress"

val defaultView = ServerDrivenView(NimbusCompose) { MockNavigator }

object MockNavigator: ServerDrivenNavigator {
    override fun dismiss() {}
    override fun pop() {}
    override fun popTo(url: String) {}
    override fun present(request: ViewRequest) {}
    override fun push(request: ViewRequest) {}
}

class MockNode(override val properties: Map<String, Any?>? = null): ServerDrivenNode {
    override val children: List<ServerDrivenNode>?
        get() = null
    override val component: String
        get() = DEFAULT_COMPONENT_NAME
    override val dependents: MutableSet<Dependent>
        get() = mutableSetOf()
    override var hasChanged: Boolean = true
    override val id: String
        get() = DEFAULT_COMPONENT_ID
    override var parent: Scope? = defaultView
    override val states: List<ServerDrivenState>?
        get() = null
    override fun addDependent(dependent: Dependent) {}
    override fun get(key: String): Any? = null
    override fun removeDependent(dependent: Dependent) {}
    override fun set(key: String, value: Any) {}
    override fun unset(key: String) {}
    override fun update() {}
}

class MockAction(
    override val properties: Map<String, Any?>? = null,
    override val handler: ((action: ActionTriggeredEvent) -> Unit) = {},
): ServerDrivenAction {
    override val metadata: Map<String, Any?>?
        get() = null
    override val name: String
        get() = DEFAULT_ACTION_NAME
    override fun update() {}
}

class MockEvent(
    override val actions: List<ServerDrivenAction>,
    override val node: ServerDrivenNode = MockNode(),
): ServerDrivenEvent {
    var currentStateValue: Any? = null
    override val name: String
        get() = DEFAULT_EVENT_NAME
    override val nimbus: Nimbus
        get() = NimbusCompose
    override var parent: Scope?
        get() = node
        set(value) {}
    override val states: List<ServerDrivenState>?
        get() = null
    override val view: ServerDrivenView
        get() = defaultView
    override fun get(key: String): Any? = null
    override fun run() {
        actions.forEach { it.handler(ActionTriggeredEvent(it, this, mutableSetOf())) }
    }
    override fun run(implicitStateValue: Any?) {
        currentStateValue = implicitStateValue
        run()
    }
    override fun set(key: String, value: Any) {}
    override fun unset(key: String) {}
}

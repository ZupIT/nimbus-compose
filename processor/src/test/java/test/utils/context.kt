package test.utils

import androidx.compose.runtime.Composable
import br.zup.com.nimbus.compose.ComponentData
import com.zup.nimbus.core.ActionTriggeredEvent
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.ServerDrivenConfig
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.ServerDrivenState
import com.zup.nimbus.core.ServerDrivenView
import com.zup.nimbus.core.dependency.Dependent
import com.zup.nimbus.core.log.LogLevel
import com.zup.nimbus.core.log.Logger
import com.zup.nimbus.core.network.HttpClient
import com.zup.nimbus.core.network.ServerDrivenRequest
import com.zup.nimbus.core.network.ServerDrivenResponse
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.scope.Scope
import com.zup.nimbus.core.tree.ServerDrivenAction
import com.zup.nimbus.core.tree.ServerDrivenEvent
import com.zup.nimbus.core.tree.ServerDrivenNode

const val DEFAULT_COMPONENT_NAME = "test:component"
const val DEFAULT_COMPONENT_ID = "test"
const val DEFAULT_ACTION_NAME = "test:action"
const val DEFAULT_EVENT_NAME = "onPress"

val defaultNimbus = Nimbus(ServerDrivenConfig(
    "",
    "test",
    httpClient = MockHttpClient,
    logger = MockLogger,
))
val defaultView = ServerDrivenView(defaultNimbus) { MockNavigator }
val defaultChildren = @Composable {}

object MockNavigator: ServerDrivenNavigator {
    override fun dismiss() {}
    override fun pop() {}
    override fun popTo(url: String) {}
    override fun present(request: ViewRequest) {}
    override fun push(request: ViewRequest) {}
}

object MockLogger: Logger {
    var errors = mutableListOf<String>()
    var warnings = mutableListOf<String>()
    var infos = mutableListOf<String>()
    override fun disable() {}
    override fun enable() {}
    override fun error(message: String) { errors.add(message) }
    override fun info(message: String) { infos.add(message) }
    override fun warn(message: String) { warnings.add(message) }

    override fun log(message: String, level: LogLevel) {
        when(level) {
            LogLevel.Error -> error(message)
            LogLevel.Info -> info(message)
            LogLevel.Warning -> warn(message)
        }
    }

    fun clear() {
        errors = mutableListOf()
        infos = mutableListOf()
        warnings = mutableListOf()
    }
}

object MockHttpClient: HttpClient {
    override suspend fun sendRequest(request: ServerDrivenRequest): ServerDrivenResponse {
        throw NotImplementedError("No need to implement this for KSP testing.")
    }
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
    override val name: String
        get() = DEFAULT_EVENT_NAME
    override val nimbus: Nimbus
        get() = defaultNimbus
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
    override fun run(implicitStateValue: Any?) = run()
    override fun set(key: String, value: Any) {}
    override fun unset(key: String) {}
}

fun createComponentData(properties: Map<String, Any?>? = null): ComponentData {
    return ComponentData(MockNode(properties), defaultChildren)
}


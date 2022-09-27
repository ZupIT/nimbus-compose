package br.zup.com.nimbus.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import com.zup.nimbus.core.dependency.Dependent
import com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NodeState(
    private val node: ServerDrivenNode,
    private val children: List<NodeFlow>?,
) {
    operator fun component1() = node
    operator fun component2() = children
}

class NodeFlow(private val node: ServerDrivenNode): Dependent {
    private var memoizedChildren: MutableMap<String, NodeFlow> = mutableMapOf()
    private val scope = CoroutineScope(CoroutineDispatcherLib.backgroundPool)
    private val current = MutableStateFlow(NodeState(node, emptyList()))

    init {
        update()
        node.addDependent(this)
    }

    fun dispose() {
        node.removeDependent(this)
    }

    @Composable
    fun collectAsState(): State<NodeState> = current.collectAsState()

    override fun update() {
        val children = node.children?.map { child ->
            if (memoizedChildren[child.id] == null) {
                memoizedChildren[child.id] = NodeFlow(child)
            }
            memoizedChildren[child.id]!!
        }
        scope.launch {
            current.emit(NodeState(node, children))
        }
    }
}

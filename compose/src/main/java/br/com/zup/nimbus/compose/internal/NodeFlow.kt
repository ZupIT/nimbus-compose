package br.com.zup.nimbus.compose.internal

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import br.com.zup.nimbus.compose.CoroutineDispatcherLib
import br.com.zup.nimbus.core.dependency.Dependent
import br.com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class NodeState(
    private val node: ServerDrivenNode,
    private val children: List<NodeFlow>?,
) {
    operator fun component1() = node
    operator fun component2() = children
}

class NodeFlow(
    private val node: ServerDrivenNode,
    private val scope: CoroutineScope = CoroutineScope(CoroutineDispatcherLib.backgroundPool)
): Dependent {
    private var memoizedChildren: MutableMap<String, NodeFlow> = mutableMapOf()
    private val current = MutableStateFlow(NodeState(node, calculateChildren()))
    val id: String get() = node.id

    init {
        node.addDependent(this)
    }

    fun dispose() {
        node.removeDependent(this)
    }

    @Composable
    fun collectAsState(): State<NodeState> = current.collectAsState()

    private fun calculateChildren() = node.children?.map { child ->
        var result = memoizedChildren[child.id]
        if (result == null) {
            result = NodeFlow(child)
            memoizedChildren[child.id] = result
        }
        result
    }

    override fun update() {
        val children = calculateChildren()
        scope.launch {
            current.emit(NodeState(node, children))
        }
    }
}

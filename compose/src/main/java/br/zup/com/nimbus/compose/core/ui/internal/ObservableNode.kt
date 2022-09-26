package br.zup.com.nimbus.compose.core.ui.internal

import com.zup.nimbus.core.dependency.Dependent
import com.zup.nimbus.core.tree.ServerDrivenNode

class ObservableNode(
    val node: ServerDrivenNode,
): Dependent {
    var children: List<ObservableNode>? = null

    private var memoizedChildren: MutableMap<String, ObservableNode> = mutableMapOf()
    private var listener: (() -> Unit)? = null

    // hack: the only purpose of this variable is to help forcing the RenderedNode to update.
    var forceUpdate = false

    init {
        update()
        node.addDependent(this)
        // we might want to remove this dependency when the node ceases to exist.
    }

    fun onChange(listener: () -> Unit) {
        this.listener = listener
    }

    override fun update() {
        children = node.children?.map { child ->
            if (memoizedChildren[child.id] == null) {
                memoizedChildren[child.id] = ObservableNode(child)
            }
            memoizedChildren[child.id]!!
        }
        forceUpdate = !forceUpdate
        listener?.let { it() }
    }
}

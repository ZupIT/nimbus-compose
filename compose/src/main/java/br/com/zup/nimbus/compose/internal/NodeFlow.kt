/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import br.com.zup.nimbus.compose.CoroutineDispatcherLib
import br.com.zup.nimbus.core.dependency.Dependent
import br.com.zup.nimbus.core.tree.ServerDrivenNode
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
    private val current = MutableStateFlow(NodeState(node, calculateChildren()))
    // whether this state is attached to the dependency graph or not
    private var isAttached = false
    val id: String get() = node.id

    /**
     * Attaches this state to Nimbus dependency graph if it's not yet attached.
     * This allows the state to respond to updates from Nimbus.
     */
    fun attach() {
        if (!isAttached) {
            node.addDependent(this)
            isAttached = true
        }
    }

    /**
     * Detaches this state from Nimbus dependency graph if it is attached.
     * This makes this state stop responding to updates from Nimbus.
     */
    fun detach() {
        if (isAttached) {
            node.removeDependent(this)
            isAttached = false
        }
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

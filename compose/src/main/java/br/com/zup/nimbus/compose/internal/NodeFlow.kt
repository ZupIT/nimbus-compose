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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import br.com.zup.nimbus.compose.CoroutineDispatcherLib
import br.com.zup.nimbus.core.dependency.Dependent
import br.com.zup.nimbus.core.tree.ServerDrivenNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

@Stable
class NodeState(
    private val node: ServerDrivenNode,
    private val children: List<NodeFlow>?,
) {
    operator fun component1() = node
    operator fun component2() = children
    private val hash = Random.nextInt()

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return hash
    }
}

/**
 * We might need to review this.
 *
 * NodeFlow will always be the same object, even if a component leaves the tree and comes back. A
 * drawback is that a NodeFlow will only leave the memory when the parent is destroyed. This can
 * be a memory hog for very dynamic lists that destroys many nodes and create many new ones. The
 * NodeFlows for the destroyed nodes would be kept in memory even though they're not needed.
 *
 * I've already tried once to make NodeFlow not persist. It would be recreated everytime it comes
 * back into the tree. But for some reason Compose will not recognize two different instances of
 * NodeFlow as being the same thing, even if equals is implemented. The result is that it starts
 * recomposing every descendant of any component that is updated, which hinges performance, by a
 * lot.
 */
@Immutable
class NodeFlow(private val node: ServerDrivenNode): Dependent {
    private var memoizedChildren: MutableMap<String, NodeFlow> = mutableMapOf()
    private val scope = CoroutineScope(CoroutineDispatcherLib.backgroundPool)
    private val current = MutableStateFlow(NodeState(node, calculateChildren()))
    val id: String get() = node.id
    private var isSubscribed = false

    fun subscribe() {
        if (isSubscribed) return
        node.addDependent(this)
        isSubscribed = true
    }

    fun unsubscribe() {
        if (!isSubscribed) return
        node.removeDependent(this)
        isSubscribed = false
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

    // fixme: why does it always get called twice?
    override fun update() {
        val children = calculateChildren()
        scope.launch {
            current.emit(NodeState(node, children))
        }
    }
}

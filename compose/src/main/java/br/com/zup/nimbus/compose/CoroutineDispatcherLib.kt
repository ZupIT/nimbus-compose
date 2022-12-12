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

package br.com.zup.nimbus.compose

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlin.properties.Delegates

private const val SHARED_FLOW_REPLAY_COUNT = 5
internal object CoroutineDispatcherLib {
    lateinit var backgroundPool: CoroutineDispatcher
    lateinit var inputOutputPool: CoroutineDispatcher
    lateinit var mainThread: CoroutineDispatcher
    var REPLAY_COUNT by Delegates.notNull<Int>()
    lateinit var ON_BUFFER_OVERFLOW: BufferOverflow

    init {
        reset()
    }

    fun reset() {
        backgroundPool = Dispatchers.IO
        mainThread = Dispatchers.Main
        inputOutputPool = Dispatchers.Default
        REPLAY_COUNT = SHARED_FLOW_REPLAY_COUNT
        ON_BUFFER_OVERFLOW = BufferOverflow.DROP_OLDEST
    }
}

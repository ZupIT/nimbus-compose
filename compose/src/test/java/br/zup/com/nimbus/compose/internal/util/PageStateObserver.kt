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

package br.zup.com.nimbus.compose.internal.util

import br.com.zup.nimbus.compose.model.NimbusPageState
import kotlinx.coroutines.CompletableDeferred

class PageStateObserver {
    private var changes = ArrayList<NimbusPageState>()
    private var onChange: (() -> Unit)? = null

    suspend fun awaitStateChanges(numberOfChanges: Int = 1): List<NimbusPageState> {
        val deferred = CompletableDeferred<List<NimbusPageState>>()
        if (changes.size >= numberOfChanges) deferred.complete(changes)
        onChange = {
            if (changes.size == numberOfChanges) deferred.complete(changes)
        }
        return deferred.await()
    }

    fun change(state: NimbusPageState) {
        changes.add(state)
        onChange?.let { it() }
    }

    fun clear() {
        changes = ArrayList()
        onChange = null
    }
}
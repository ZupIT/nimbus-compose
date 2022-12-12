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

package br.com.zup.nimbus.compose.ui

import br.com.zup.nimbus.core.ActionHandler
import br.com.zup.nimbus.core.ActionInitializationHandler
import br.com.zup.nimbus.core.OperationHandler
import br.com.zup.nimbus.core.ui.UILibrary
import br.com.zup.nimbus.compose.ComponentHandler

class NimbusComposeUILibrary(namespace: String = ""): UILibrary(namespace) {
    private val components = HashMap<String, ComponentHandler>()

    fun addComponent(name: String, handler: ComponentHandler): NimbusComposeUILibrary {
        components[name] = handler
        return this
    }

    fun getComponent(name: String): ComponentHandler? {
        return components[name]
    }

    override fun addAction(name: String, handler: ActionHandler): NimbusComposeUILibrary {
        super.addAction(name, handler)
        return this
    }

    override fun addActionInitializer(
        name: String,
        handler: ActionInitializationHandler,
    ): NimbusComposeUILibrary {
        super.addActionInitializer(name, handler)
        return this
    }

    override fun addActionObserver(observer: ActionHandler): NimbusComposeUILibrary {
        super.addActionObserver(observer)
        return this
    }

    override fun addOperation(name: String, handler: OperationHandler): NimbusComposeUILibrary {
        super.addOperation(name, handler)
        return this
    }

    fun merge(other: NimbusComposeUILibrary): NimbusComposeUILibrary {
        super.merge(other)
        components.putAll(other.components)
        return this
    }
}

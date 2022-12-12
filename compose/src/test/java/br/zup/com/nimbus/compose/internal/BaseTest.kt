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

package br.zup.com.nimbus.compose.internal

import br.com.zup.nimbus.compose.Nimbus
import br.com.zup.nimbus.compose.internal.PagesManager
import br.com.zup.nimbus.core.network.ViewClient
import br.com.zup.nimbus.core.tree.dynamic.node.RootNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

const val BASE_URL = "http://localhost"
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {
    internal val nimbusConfig: Nimbus = mockk()
    internal val pagesManager: PagesManager = mockk()
    private val viewClient: ViewClient = mockk()

    @BeforeAll
    open fun setUp() {
        mockNimbusConfig()
    }

    @AfterAll
    open fun tearDown() {
        unmockkAll()
    }

    private fun mockNimbusConfig(){
        every { nimbusConfig.baseUrl } returns BASE_URL
        every { nimbusConfig.viewClient } returns viewClient
    }

    internal fun shouldEmitRenderNodeFromCore(node: RootNode) {
        coEvery { nimbusConfig.viewClient.fetch(any()) } returns node
        every { nimbusConfig.nodeBuilder.buildFromJsonString(any()) } returns node
    }

    internal fun shouldEmitExceptionFromCore(expectedException: Throwable) {
        coEvery { nimbusConfig.viewClient.fetch(any()) } throws expectedException
        every { nimbusConfig.nodeBuilder.buildFromJsonString(any()) } throws expectedException
    }

}
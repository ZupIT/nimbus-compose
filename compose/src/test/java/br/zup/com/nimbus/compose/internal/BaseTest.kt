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
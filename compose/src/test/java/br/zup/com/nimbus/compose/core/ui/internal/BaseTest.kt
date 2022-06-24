package br.zup.com.nimbus.compose.core.ui.internal

import br.zup.com.nimbus.compose.NimbusConfig
import com.zup.nimbus.core.Nimbus
import com.zup.nimbus.core.network.ViewClient
import com.zup.nimbus.core.tree.RenderNode
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
    internal val nimbusConfig: NimbusConfig = mockk()
    internal val nimbusCore: Nimbus = mockk()
    internal val pagesManager: PagesManager = mockk()
    internal val viewClient: ViewClient = mockk()
    internal val renderNode: RenderNode = mockk()

    @BeforeAll
    open fun setUp() {
        mockNimbusConfig()
    }

    @AfterAll
    open fun tearDown() {
        unmockkAll()
    }

    protected fun mockNimbusConfig(){
        every { nimbusConfig.actions } returns mapOf()
        every { nimbusConfig.baseUrl } returns BASE_URL
        every { nimbusConfig.core } returns nimbusCore
        every { nimbusConfig.core.viewClient} returns viewClient
    }

    internal fun shouldEmitRenderNodeFromCore(renderNode: RenderNode) {
        coEvery { nimbusConfig.core.viewClient.fetch(any()) } returns renderNode
        every { nimbusConfig.core.createNodeFromJson(any()) } returns renderNode
    }

    internal fun shouldEmitExceptionFromCore(expectedException: Throwable) {
        coEvery { nimbusConfig.core.viewClient.fetch(any()) } throws expectedException
        every { nimbusConfig.core.createNodeFromJson(any()) } throws expectedException
    }

}
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
    internal val nimbusConfig: NimbusConfig = mockk(relaxed = true, relaxUnitFun = true)
    internal val nimbusCore: Nimbus = mockk(relaxed = true, relaxUnitFun = true)
    internal val pagesManager: PagesManager = mockk()
    internal val viewClient: ViewClient = mockk(relaxed = true, relaxUnitFun = true)
    internal val renderNode: RenderNode = mockk(relaxed = true, relaxUnitFun = true)

    @BeforeAll
    open fun setUp() {
        mockBeagleEnvironment()
    }

    @AfterAll
    open fun tearDown() {
        unmockkAll()
    }

    protected fun mockBeagleEnvironment(){
        every { nimbusConfig.actions } returns mapOf()
        every { nimbusConfig.baseUrl } returns BASE_URL
        every { nimbusConfig.core } returns nimbusCore
        every { nimbusConfig.core.viewClient} returns viewClient
        coEvery { nimbusConfig.core.viewClient.fetch(any()) } returns renderNode
        //TODO fill in the other properties
    }
}
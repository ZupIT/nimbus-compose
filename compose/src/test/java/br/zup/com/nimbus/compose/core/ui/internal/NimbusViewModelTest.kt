package br.zup.com.nimbus.compose.core.ui.internal

import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.Listener
import com.zup.nimbus.core.render.Renderer
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

//@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@ExtendWith(CoroutinesTestExtension::class)
class NimbusViewModelTest : BaseTest() {
    private val serverDrivenView: ServerDrivenView = mockk()
    private val serverDrivenNode: ServerDrivenNode = mockk()
    private val renderer: Renderer = mockk()
    private lateinit var viewModel: NimbusViewModel

    private val captureSlotTree = slot<Listener>()
    private val slotPage = mutableListOf<Page>()
    private val slotPageState = mutableListOf<NimbusPageState>()

    @BeforeEach
    fun before() {
        viewModel = NimbusViewModel(nimbusConfig = nimbusConfig, pagesManager = pagesManager)
        every { pagesManager.add(capture(slotPage)) } answers { true }
        every { nimbusConfig.core.createView(any(), any()) } returns serverDrivenView
        every { serverDrivenView.renderer } returns renderer
        every { renderer.paint(any()) } just Runs
        every { serverDrivenView.onChange(capture(captureSlotTree)) } answers { serverDrivenNode }
    }

    @DisplayName("When initFirstViewWithRequest")
    @Nested
    inner class ViewWithRequest {
        @OptIn(ExperimentalCoroutinesApi::class)
        @DisplayName("Then should post PageStateOnLoading and PageStateOnShowPage")
        @Test
        @Suppress("UNCHECKED_CAST")
        fun testGivenAViewRequestWhenInitFirstViewShouldLoadingOnShowPage() = runTest {

            // Given
            val expectedEmissionCount = 2
            val viewRequest = ViewRequest(url = RandomData.httpUrl())


            //When
            viewModel.initFirstViewWithRequest(viewRequest)

            val page = slotPage.first()

            emitServerDrivenNode(serverDrivenNode)

            page.content.take(expectedEmissionCount).collect {
                slotPageState.add(it)
            }

            //Then
            assertEquals(slotPageState.first(), NimbusPageState.PageStateOnLoading)
            assertEquals((slotPageState.last() as NimbusPageState.PageStateOnShowPage).serverDrivenNode,
                serverDrivenNode)
        }
    }

    private fun emitServerDrivenNode(serverDrivenNode: ServerDrivenNode) {
        val list = captureSlotTree.captured
        list.invoke(serverDrivenNode)
    }
}

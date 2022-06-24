package br.zup.com.nimbus.compose.core.ui.internal

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import br.zup.com.nimbus.compose.core.ui.internal.util.CoroutinesTestExtension
import br.zup.com.nimbus.compose.core.ui.internal.util.RandomData
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.Listener
import com.zup.nimbus.core.render.Renderer
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
@ExtendWith(CoroutinesTestExtension::class)
class NimbusViewModelTest : BaseTest() {
    private val serverDrivenView: ServerDrivenView = mockk()
    private val serverDrivenNode: ServerDrivenNode = mockk()
    private val renderer: Renderer = mockk()
    private lateinit var viewModel: NimbusViewModel

    //Slots
    private val pageOnChangeSlot = slot<Listener>()
    private val pageManagerAddSlot = mutableListOf<Page>()

    @BeforeEach
    fun before() {
        viewModel = NimbusViewModel(nimbusConfig = nimbusConfig, pagesManager = pagesManager)
        every { pagesManager.add(capture(pageManagerAddSlot)) } answers { true }
        every { nimbusConfig.core.createView(any(), any()) } returns serverDrivenView
        every { serverDrivenView.renderer } returns renderer
        every { renderer.paint(any()) } just Runs
        every { serverDrivenView.onChange(capture(pageOnChangeSlot)) } answers { serverDrivenNode }

        pageManagerAddSlot.clear()
        pageOnChangeSlot.clear()
    }

    @DisplayName("When initFirstViewWithRequest")
    @Nested
    inner class ViewWithRequest {
        @DisplayName("Then should post PageStateOnLoading and PageStateOnShowPage")
        @Test
        @Suppress("UNCHECKED_CAST")
        fun testGivenAViewRequestWhenInitFirstViewShouldLoadingOnShowPage() = runTest {

            // Given
            val viewRequest = ViewRequest(url = RandomData.httpUrl())
            val expectedFirstEmission = NimbusPageState.PageStateOnLoading
            val expectedSecondEmission = NimbusPageState.PageStateOnShowPage(serverDrivenNode)

            //When
            viewModel.initFirstViewWithRequest(viewRequest)

            val page = pageManagerAddSlot.last()

            emitOnChangeServerDrivenNode(serverDrivenNode)

            //Then
            page.content.test {
                assertEquals(expectedFirstEmission, awaitItem())
                assertEquals(expectedSecondEmission, awaitItem())
            }
        }

        @DisplayName("Then should post PageStateOnLoading and PageStateOnError then retry with success")
        @Test
        @Suppress("UNCHECKED_CAST")
        fun testGivenAViewRequestWhenInitFirstViewShouldPageStateOnError() = runTest {

            // Given
            val viewRequest = ViewRequest(url = RandomData.httpUrl())
            val expectedException = RuntimeException("Any Exception")
            val expectedLoading = NimbusPageState.PageStateOnLoading
            val expectedPageError = NimbusPageState.PageStateOnError(throwable = expectedException, retry = {})
            val expectedOnShowPage = NimbusPageState.PageStateOnShowPage(serverDrivenNode)


            coEvery { nimbusConfig.core.viewClient.fetch(any()) } throws expectedException

            //When
            viewModel.initFirstViewWithRequest(viewRequest)

            val page = pageManagerAddSlot.last()

            //Then
            page.content.test {
                val secondItem = shouldBeLoadingAndError(expectedLoading, expectedPageError)

                shouldBeLoadingAndShowPage(secondItem, expectedLoading, expectedOnShowPage)
            }
        }

        private suspend fun FlowTurbine<NimbusPageState>.shouldBeLoadingAndShowPage(
            secondItem: NimbusPageState.PageStateOnError,
            expectedLoading: NimbusPageState.PageStateOnLoading,
            expectedOnShowPage: NimbusPageState.PageStateOnShowPage,
        ) {
            coEvery { nimbusConfig.core.viewClient.fetch(any()) } returns renderNode

            //Simulates user clicking retry button on error screen
            secondItem.retry.invoke()
            emitOnChangeServerDrivenNode(serverDrivenNode)

            val thirdItem = awaitItem()
            assertEquals(expectedLoading, thirdItem)

            val fourthItem = awaitItem()
            assertEquals(expectedOnShowPage, fourthItem)
        }

        private suspend fun FlowTurbine<NimbusPageState>.shouldBeLoadingAndError(
            expectedLoading: NimbusPageState.PageStateOnLoading,
            expectedPageError: NimbusPageState.PageStateOnError,
        ): NimbusPageState.PageStateOnError {
            val firstItem = awaitItem()
            assertEquals(expectedLoading, firstItem)

            val secondItem = (awaitItem() as NimbusPageState.PageStateOnError)
            assertEquals(expectedPageError.throwable,
                secondItem.throwable)
            return secondItem
        }
    }

    private fun emitOnChangeServerDrivenNode(serverDrivenNode: ServerDrivenNode) {
        val list = pageOnChangeSlot.captured
        list.invoke(serverDrivenNode)
    }
}

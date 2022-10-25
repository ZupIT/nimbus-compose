package br.zup.com.nimbus.compose.internal

import app.cash.turbine.test
import br.zup.com.nimbus.compose.internal.util.CoroutinesTestExtension
import br.zup.com.nimbus.compose.internal.util.PageStateObserver
import br.zup.com.nimbus.compose.internal.util.RandomData
import br.zup.com.nimbus.compose.internal.util.RandomData.jsonExample
import br.zup.com.nimbus.compose.internal.util.invokeHiddenMethod
import br.zup.com.nimbus.compose.internal.util.observe
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.ServerDrivenView
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.tree.dynamic.node.RootNode
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(CoroutinesTestExtension::class)
class NimbusViewModelTest : BaseTest() {
    private val serverDrivenNode: RootNode = mockk()
    private val page: Page = mockk()
    private lateinit var viewModel: NimbusViewModel

    //Slots
    private val pageOnChangeSlot = slot<((NimbusPageState) -> Unit)>()
    private val pageObserverSlot = mutableListOf<PageStateObserver>()
    lateinit var serverDrivenNavigatorSlot: ServerDrivenNavigator

    @BeforeEach
    fun before() {
        viewModel = NimbusViewModel(nimbusConfig = nimbusConfig, pagesManager = pagesManager)
        every { pagesManager.add(any()) } answers {
            val page = this.arg<Page>(0)
            pageObserverSlot.add(page.observe())
            true
        }
        val viewSlot = slot<ServerDrivenView>()
        every { serverDrivenNode.initialize(capture(viewSlot)) } answers {
            serverDrivenNavigatorSlot = viewSlot.captured.navigator
        }
        every { pagesManager.popLastPage() } returns true

        clearMocks(pagesManager, answers = false)
        pageObserverSlot.clear()
        pageOnChangeSlot.clear()
    }

    @DisplayName("When initFirstViewWithRequest")
    @Nested
    inner class ViewWithRequest {
        @DisplayName("Then should post PageStateOnLoading and PageStateOnShowPage")
        @Test
        fun testGivenAViewRequestWhenInitFirstViewShouldLoadingOnShowPage() = runTest {
            val expectedState = NimbusPageState.PageStateOnShowPage(serverDrivenNode)
            val observer = initFirstViewWithSuccess()

            //Then
            val stateHistory = observer.awaitStateChanges(2)
            assertEquals(NimbusPageState.PageStateOnLoading, stateHistory[0])
            assertEquals(expectedState, stateHistory[1])
            verify(exactly = 1) { pagesManager.add(any()) }
        }

        @DisplayName("Then should post PageStateOnLoading and PageStateOnError then retry with success")
        @Test
        fun testGivenAViewRequestWhenInitFirstViewShouldPageStateOnError() = runTest {
            // Given
            val viewRequest = ViewRequest(url = RandomData.httpUrl())
            val expectedException = RuntimeException("Any Exception")
            val expectedLoading = NimbusPageState.PageStateOnLoading
            val expectedPageError =
                NimbusPageState.PageStateOnError(throwable = expectedException, retry = {})
            val expectedOnShowPage = NimbusPageState.PageStateOnShowPage(serverDrivenNode)

            coEvery { nimbusConfig.viewClient.fetch(any()) } throws expectedException

            //When
            viewModel.initFirstViewWithRequest(viewRequest)
            val observer = pageObserverSlot.last()

            //Then
            val errorState = shouldEmitLoadingAndError(observer, expectedPageError)
            shouldEmitLoadingAndShowPageAfterRetry(
                observer, errorState, expectedLoading, expectedOnShowPage,
            )
            verify(exactly = 1) { pagesManager.add(any()) }
        }
    }

    private fun initFirstViewWithSuccess(): PageStateObserver {
        // Given
        val viewRequest = ViewRequest(url = RandomData.httpUrl())

        shouldEmitRenderNodeFromCore(serverDrivenNode)

        //When
        viewModel.initFirstViewWithRequest(viewRequest)
        return pageObserverSlot.last()
    }

    @DisplayName("When initFirstViewWithJson")
    @Nested
    inner class ViewWithJson {
        @DisplayName("Then should post PageStateOnLoading and PageStateOnShowPage")
        @Test
        fun testGivenAJsonWhenInitFirstViewShouldLoadingOnShowPage() = runTest {
            // Given
            val json = jsonExample()
            val expectedFirstEmission = NimbusPageState.PageStateOnLoading
            val expectedSecondEmission = NimbusPageState.PageStateOnShowPage(serverDrivenNode)
            shouldEmitRenderNodeFromCore(serverDrivenNode)

            //When
            viewModel.initFirstViewWithJson(json)

            val observer = pageObserverSlot.last()

            //emitOnChangeServerDrivenNode(serverDrivenNode)

            //Then
            val stateHistory = observer.awaitStateChanges(2)
            assertEquals(expectedFirstEmission, stateHistory[0])
            assertEquals(expectedSecondEmission, stateHistory[1])
            verify(exactly = 1) { pagesManager.add(any()) }
        }

        @DisplayName("Then should post PageStateOnLoading and PageStateOnError then retry with success")
        @Test
        fun testGivenAJsonWhenInitFirstViewShouldPageStateOnError() = runTest {
            // Given
            val json = jsonExample()
            val expectedException = RuntimeException("Any Exception")
            val expectedLoading = NimbusPageState.PageStateOnLoading
            val expectedPageError =
                NimbusPageState.PageStateOnError(throwable = expectedException, retry = {})
            val expectedOnShowPage = NimbusPageState.PageStateOnShowPage(serverDrivenNode)

            shouldEmitExceptionFromCore(expectedException)

            //When
            viewModel.initFirstViewWithJson(json)

            val observer = pageObserverSlot.last()

            //Then
            val secondItem = shouldEmitLoadingAndError(observer, expectedPageError)
            shouldEmitLoadingAndShowPageAfterRetry(
                observer, secondItem, expectedLoading, expectedOnShowPage,
            )
            verify(exactly = 1) { pagesManager.add(any()) }
        }
    }

    @DisplayName("When receive a ServerDrivenNavigator event")
    @Nested
    inner class Navigator {
        @DisplayName("Then should post NimbusViewModelNavigationState.Pop")
        @Test
        fun testGivenAPopNavigationEventShouldPostNimbusViewModelNavigationStatePop() = runTest {
            val url = RandomData.httpUrl()
            val secondViewRequest = ViewRequest(url)
            //Given
            val expectedFirstEmission = NimbusPageState.PageStateOnLoading
            val expectedSecondEmission = NimbusPageState.PageStateOnShowPage(serverDrivenNode)
            val expectedNavigationFirstEmission = NimbusViewModelNavigationState.Push(
                url)
            val expectedNavigationSecondEmission = NimbusViewModelNavigationState.Pop

            //When
            val observer = initFirstViewWithSuccess()

            //Then
            val stateHistory = observer.awaitStateChanges(2)
            assertEquals(expectedFirstEmission, stateHistory[0])
            assertEquals(expectedSecondEmission, stateHistory[1])

            verify(exactly = 1) { pagesManager.add(any()) }

            //After that push another view and pops it
            serverDrivenNavigatorSlot.push(secondViewRequest)
            serverDrivenNavigatorSlot.pop()

            viewModel.nimbusViewNavigationState.test {
                assertEquals(expectedNavigationFirstEmission, awaitItem())
                assertEquals(expectedNavigationSecondEmission, awaitItem())
            }

            verify(exactly = 2) { pagesManager.add(any()) }
            verify(exactly = 1) { pagesManager.popLastPage() }
        }

        @DisplayName("Then should post NimbusViewModelNavigationState.OnShowModalModalState")
        @Test
        fun testGivenAPresentNavigationEventShouldPostOnShowModalModalState() = runTest {
            //Given
            val url = RandomData.httpUrl()
            val viewRequest = ViewRequest(url)
            val expectedShowModal = NimbusViewModelModalState.OnShowModalModalState(viewRequest)
            val expectedHideModal = NimbusViewModelModalState.OnHideModalState

            //When
            initFirstViewWithSuccess()
            serverDrivenNavigatorSlot.present(viewRequest)
            viewModel.nimbusViewModelModalState.test {
                assertEquals(expectedShowModal, awaitItem())
            }
            //When
            serverDrivenNavigatorSlot.dismiss()
            //Then
            viewModel.nimbusViewModelModalState.test {
                assertEquals(expectedHideModal, awaitItem())
            }

        }

        @DisplayName("Then should post NimbusViewModelNavigationState.PopTo(url)")
        @Test
        fun testGivenAPopToUrlEventShouldPostPopTo() = runTest {
            //Given
            val url = RandomData.httpUrl()
            val expectedState = NimbusViewModelNavigationState.PopTo(url)

            every {  pagesManager.getPageBy(url) } returns page
            every {  pagesManager.removePagesAfter(page) } just Runs

            //When
            initFirstViewWithSuccess()
            serverDrivenNavigatorSlot.popTo(url)

            //Then
            viewModel.nimbusViewNavigationState.test {
                assertEquals(expectedState, awaitItem())
            }

        }
    }

    suspend fun shouldEmitLoadingAndShowPageAfterRetry(
        observer: PageStateObserver,
        secondItem: NimbusPageState.PageStateOnError,
        expectedLoading: NimbusPageState.PageStateOnLoading,
        expectedOnShowPage: NimbusPageState.PageStateOnShowPage,
    ) {
        observer.clear()
        shouldEmitRenderNodeFromCore(serverDrivenNode)
        //Simulates user clicking retry button on error screen
        secondItem.retry.invoke()
        val stateHistory = observer.awaitStateChanges(2)
        assertEquals(expectedLoading, stateHistory[0])
        assertEquals(expectedOnShowPage, stateHistory[1])
    }

    @DisplayName("When receive a view model method call")
    @Nested
    inner class ViewModel {
        @DisplayName("Then should return false when in first page")
        @Test
        fun testGivenAPopWithOnlyOnePageShouldReturnFalse() = runTest {
            val expectedPop = false

            //Given
            every { pagesManager.popLastPage() } returns false

            //When
            val pop = viewModel.pop()

            //Then
            verify(exactly = 1) { pagesManager.popLastPage() }
            assertEquals(expectedPop , pop)
        }

        @DisplayName("Then should return the page when getPageBy")
        @Test
        fun testGivenAGetPageByShouldReturnThePage() = runTest {
            val expectedPage = page
            val url = RandomData.httpUrl()

            //Given
            every { pagesManager.getPageBy(url) } returns page

            //When
            val page = viewModel.getPageBy(url)

            //Then
            verify(exactly = 1) { pagesManager.getPageBy(url) }
            assertEquals(expectedPage , page)
        }

        @DisplayName("Then should return the page count when getPageCount")
        @Test
        fun testGivenAGetPageCountShouldReturnThePageCount() = runTest {
            val expectedPageCount = RandomData.int()

            //Given
            every { pagesManager.getPageCount() } returns expectedPageCount

            //When
            val result = viewModel.getPageCount()

            //Then
            verify(exactly = 1) { pagesManager.getPageCount() }
            assertEquals(expectedPageCount , result)
        }

        @DisplayName("Then should dispose the pages")
        @Test
        fun testGivenDisposeCallShouldDisposePages() = runTest {

            //Given
            every { pagesManager.removeAllPages() } just Runs

            //When
            viewModel.dispose()

            //Then
            verify(exactly = 1) { pagesManager.removeAllPages() }
        }

        @DisplayName("Then should dispose the pages when cleared")
        @Test
        fun testGivenClearedShouldDisposePages() = runTest {
            //Given
            every { pagesManager.removeAllPages() } just Runs

            //When
            viewModel.invokeHiddenMethod("onCleared")

            //Then
            verify(exactly = 1) { pagesManager.removeAllPages() }
        }
    }

    @DisplayName("When receive an event to change model to hidden state")
    @Nested
    inner class ViewModelModalState {
        @DisplayName("Then should return receive a hidden state emission")
        @Test
        fun testGivenAPopWithOnlyOnePageShouldReturnFalse() = runTest {
            val expectedModalState = NimbusViewModelModalState.HiddenModalState

            //When
            viewModel.setModalHiddenState()

            //Then
            viewModel.nimbusViewModelModalState.test {
                assertEquals(expectedModalState, awaitItem())
            }
        }
    }

    private suspend fun shouldEmitLoadingAndError(
        observer: PageStateObserver,
        expectedPageError: NimbusPageState.PageStateOnError,
    ): NimbusPageState.PageStateOnError {
        val stateHistory = observer.awaitStateChanges(2)
        assertEquals(NimbusPageState.PageStateOnLoading, stateHistory[0])
        val errorState = stateHistory[1] as NimbusPageState.PageStateOnError
        assertEquals(expectedPageError.throwable, errorState.throwable)
        return errorState
    }
}

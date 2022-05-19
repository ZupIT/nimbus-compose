package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.zup.com.nimbus.compose.NimbusConfig
import br.zup.com.nimbus.compose.VIEW_INITIAL_URL
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
import br.zup.com.nimbus.compose.model.removeAllPages
import br.zup.com.nimbus.compose.model.removeLastPage
import br.zup.com.nimbus.compose.model.removePagesAfter
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.network.ViewRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal sealed class NimbusViewModelModalState {
    object HiddenModalState : NimbusViewModelModalState()
    object OnHideModalState : NimbusViewModelModalState()
    class OnShowModalModalState(val viewRequest: ViewRequest) : NimbusViewModelModalState()
}

@Suppress("CanSealedSubClassBeObject")
internal sealed class NimbusViewModelNavigationState {
    object RootState : NimbusViewModelNavigationState()
    class Push(val url: String) : NimbusViewModelNavigationState()
    class Pop : NimbusViewModelNavigationState()
    class PopTo(val url: String) : NimbusViewModelNavigationState()
}

internal class NimbusViewModel(
    private val nimbusConfig: NimbusConfig,
) : ViewModel() {

    private val pages = ArrayList<Page>()
    var nimbusViewModelModalState: NimbusViewModelModalState by
    mutableStateOf(NimbusViewModelModalState.HiddenModalState)
        private set

    private var _nimbusViewNavigationState: MutableStateFlow<NimbusViewModelNavigationState> =
        MutableStateFlow(NimbusViewModelNavigationState.RootState)

    val nimbusViewNavigationState: StateFlow<NimbusViewModelNavigationState>
        get() = _nimbusViewNavigationState

    companion object {
        fun provideFactory(
            nimbusConfig: NimbusConfig,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NimbusViewModel(
                    nimbusConfig = nimbusConfig
                ) as T
            }
        }
    }

    private val serverDrivenNavigator: ServerDrivenNavigator = object : ServerDrivenNavigator {
        override fun dismiss() {
            nimbusViewModelModalState = NimbusViewModelModalState.OnHideModalState
        }

        override fun popTo(url: String) {
            popNavigationTo(url)
        }

        override fun present(request: ViewRequest) {
            nimbusViewModelModalState = NimbusViewModelModalState.OnShowModalModalState(request)
        }

        override fun pop() {
            popNavigation()
        }

        override fun push(request: ViewRequest) {
            doPushWithViewRequest(request)
        }
    }

    fun setModalHiddenState() {
        nimbusViewModelModalState = NimbusViewModelModalState.HiddenModalState
    }

    fun pop(): Boolean {
        if (pages.size <= 1) {
            return false
        }

        removeLastPage()

        setNavigationState(NimbusViewModelNavigationState.Pop())

        return true
    }

    fun initFirstViewWithRequest(viewRequest: ViewRequest) {
        doPushWithViewRequest(viewRequest, initialRequest = true)
    }

    fun initFirstViewWithJson(json: String) {
        doPushWithJson(json)
    }

    fun getPageBy(url: String): Page? {
        return pages.firstOrNull { it.id == url }
    }

    fun dispose() {
        removeAllPages()
    }

    override fun onCleared() {
        super.onCleared()
        //Cannot call coroutines at this moment, should run everything on main thread
        pages.removeAllPages()
    }

    private fun setNavigationState(nimbusViewModelNavigationState: NimbusViewModelNavigationState) = viewModelScope.launch(Dispatchers.IO) {
        _nimbusViewNavigationState.value = nimbusViewModelNavigationState
    }

    private fun pushNavigation(page: Page, initialRequest: Boolean) =
        viewModelScope.launch(Dispatchers.Default) {
            pages.add(page)
            if (!initialRequest) {
                setNavigationState(NimbusViewModelNavigationState.Push(page.id))
            }
        }

    private fun popNavigation() = viewModelScope.launch(Dispatchers.IO) {
        pop()
    }

    private fun removeAllPages() = viewModelScope.launch(Dispatchers.IO) {
        pages.removeAllPages()
    }

    private fun removeLastPage() = viewModelScope.launch(Dispatchers.IO) {
        pages.removeLastPage()
    }

    private fun popNavigationTo(url: String) = viewModelScope.launch(Dispatchers.IO) {
        val page = pages.firstOrNull {
            it.id == url
        }

        page?.let {
            page.removePagesAfter(pages)
            setNavigationState(NimbusViewModelNavigationState.PopTo(url))
        }
    }

    private fun doPushWithViewRequest(request: ViewRequest, initialRequest: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            val view = nimbusConfig.core.createView(serverDrivenNavigator)
            val url = if (initialRequest) VIEW_INITIAL_URL else request.url
            val page = Page(
                id = url, view = view
            )
            pushNavigation(page, initialRequest)

            try {
                val tree = nimbusConfig.core.viewClient.fetch(request)
                view.renderer.paint(tree)
            } catch (e: Throwable) {
                page.content = NimbusPageState.PageStateOnError(
                    e
                )
            }


        }

    private fun doPushWithJson(json: String) = viewModelScope.launch(Dispatchers.IO) {
        val view = nimbusConfig.core.createView(serverDrivenNavigator)
        val url = VIEW_INITIAL_URL
        val page = Page(
            id = url, view = view
        )
        pushNavigation(page, true)

        try {
            val tree = nimbusConfig.core.createNodeFromJson(json)
            view.renderer.paint(tree)
        } catch (e: Throwable) {
            page.content = NimbusPageState.PageStateOnError(
                e
            )
        }
    }

}
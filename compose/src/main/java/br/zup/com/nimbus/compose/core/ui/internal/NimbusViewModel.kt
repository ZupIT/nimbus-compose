package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.zup.com.nimbus.compose.NimbusConfig
import br.zup.com.nimbus.compose.VIEW_INITIAL_URL
import br.zup.com.nimbus.compose.VIEW_JSON_DESCRIPTION
import br.zup.com.nimbus.compose.model.Page
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.ServerDrivenView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val pagesManager: PagesManager = PagesManager(),
) : ViewModel() {
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
            setNimbusViewModelModalState(NimbusViewModelModalState.OnHideModalState)
        }

        override fun popTo(url: String) {
            popNavigationTo(url)
        }

        override fun present(request: ViewRequest) {
            setNimbusViewModelModalState(NimbusViewModelModalState.OnShowModalModalState(request))
        }

        override fun pop() {
            popNavigation()
        }

        override fun push(request: ViewRequest) {
            doPushWithViewRequest(request)
        }
    }

    fun setModalHiddenState() {
        setNimbusViewModelModalState(NimbusViewModelModalState.HiddenModalState)
    }

    fun pop(): Boolean {
        return if (pagesManager.popLastPage()) {
            setNavigationState(NimbusViewModelNavigationState.Pop())
            true
        } else {
            false
        }
    }

    fun initFirstViewWithRequest(viewRequest: ViewRequest) {
        doPushWithViewRequest(viewRequest, initialRequest = true)
    }

    fun initFirstViewWithJson(json: String) {
        doPushWithJson(json)
    }

    fun getPageBy(url: String): Page? {
        return pagesManager.getPageBy(url)
    }

    fun getPageCount() = pagesManager.getPageCount()

    fun dispose() = viewModelScope.launch(Dispatchers.IO) {
        pagesManager.removeAllPages()
    }

    override fun onCleared() {
        super.onCleared()
        //Cannot call coroutines at this moment, should run everything on main thread
        pagesManager.removeAllPages()
    }

    private fun setNavigationState(nimbusViewModelNavigationState: NimbusViewModelNavigationState) =
        viewModelScope.launch(Dispatchers.Main) {
            _nimbusViewNavigationState.value = nimbusViewModelNavigationState
        }

    private fun setNimbusViewModelModalState(state: NimbusViewModelModalState) =
        viewModelScope.launch(Dispatchers.Main) {
            nimbusViewModelModalState = state
        }

    private fun pushNavigation(page: Page, initialRequest: Boolean) =
        viewModelScope.launch(Dispatchers.Default) {
            pagesManager.add(page)
            if (!initialRequest) {
                setNavigationState(NimbusViewModelNavigationState.Push(page.id))
            }
        }

    private fun popNavigation() = viewModelScope.launch(Dispatchers.IO) {
        pop()
    }

    private fun popNavigationTo(url: String) = viewModelScope.launch(Dispatchers.IO) {
        val page = pagesManager.findPage(url)

        page?.let {
            pagesManager.removePagesAfter(page)
            setNavigationState(NimbusViewModelNavigationState.PopTo(url))
        }
    }

    private fun doPushWithViewRequest(request: ViewRequest, initialRequest: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            val view = nimbusConfig.core.createView(
                getNavigator = { serverDrivenNavigator },
                description = request.url
            )
            val url = if (initialRequest) VIEW_INITIAL_URL else request.url
            val page = Page(
                coroutineScope = viewModelScope,
                id = url,
                view = view
            )
            pushNavigation(page, initialRequest)

            loadViewRequest(request, view, page)
        }

    private fun loadViewRequest(
        request: ViewRequest,
        view: ServerDrivenView,
        page: Page,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                page.setLoading()
                val tree = nimbusConfig.core.viewClient.fetch(request)
                view.renderer.paint(tree)
            } catch (e: Throwable) {
                page.setError(
                    throwable = e,
                    retry = {
                        loadViewRequest(
                            request = request,
                            view = view,
                            page = page
                        )
                    }
                )
            }
        }
    }

    private fun doPushWithJson(json: String) = viewModelScope.launch(Dispatchers.IO) {
        val view = nimbusConfig.core.createView(
            getNavigator = { serverDrivenNavigator },
            description = VIEW_JSON_DESCRIPTION
        )
        val url = VIEW_INITIAL_URL
        withContext(Dispatchers.Main) {
            val page = Page(
                coroutineScope = viewModelScope,
                id = url,
                view = view
            )
            pushNavigation(page, true)
            loadJson(json, view, page)
        }
    }

    private fun loadJson(
        json: String,
        view: ServerDrivenView,
        page: Page,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                page.setLoading()
                val tree = nimbusConfig.core.createNodeFromJson(json)
                view.renderer.paint(tree)
            } catch (e: Throwable) {
                page.setError(
                    throwable = e,
                    retry = {
                        loadJson(
                            json = json,
                            view = view,
                            page = page
                        )
                    }
                )
            }
        }
    }
}

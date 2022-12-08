package br.com.zup.nimbus.compose.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.zup.nimbus.compose.CoroutineDispatcherLib
import br.com.zup.nimbus.compose.JSON
import br.com.zup.nimbus.compose.Nimbus
import br.com.zup.nimbus.compose.model.Page
import br.com.zup.nimbus.core.ServerDrivenNavigator
import br.com.zup.nimbus.core.ServerDrivenState
import br.com.zup.nimbus.core.ServerDrivenView
import br.com.zup.nimbus.core.network.ViewRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal sealed class NimbusViewModelModalState {
    object RootState : NimbusViewModelModalState()
    object OnHideModalState : NimbusViewModelModalState()
    data class OnShowModalModalState(val viewRequest: ViewRequest) : NimbusViewModelModalState()
}

@Suppress("CanSealedSubClassBeObject")
internal sealed class NimbusViewModelNavigationState {
    object RootState : NimbusViewModelNavigationState()
    data class Push(val url: String) : NimbusViewModelNavigationState()
    class Pop : NimbusViewModelNavigationState()
    data class PopTo(val url: String) : NimbusViewModelNavigationState()
}

internal class NimbusViewModel(
    private val nimbusConfig: Nimbus,
    private val pagesManager: PagesManager = PagesManager(),
    viewRequest: ViewRequest? = null,
    json: String = ""
) : ViewModel() {

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

    private var _nimbusViewModelModalState: MutableStateFlow<NimbusViewModelModalState> =
        MutableStateFlow(NimbusViewModelModalState.RootState)

    val nimbusViewModelModalState: StateFlow<NimbusViewModelModalState>
        get() = _nimbusViewModelModalState


    private var _nimbusViewNavigationState: MutableStateFlow<NimbusViewModelNavigationState> =
        MutableStateFlow(NimbusViewModelNavigationState.RootState)

    val nimbusViewNavigationState: StateFlow<NimbusViewModelNavigationState>
        get() = _nimbusViewNavigationState

    init {
        if (viewRequest != null) {
            initFirstViewWithRequest(viewRequest = viewRequest)
        } else {
            initFirstViewWithJson(json = json)
        }
    }

    companion object {
        fun provideFactory(
            nimbusConfig: Nimbus,
            viewRequest: ViewRequest? = null,
            json: String = ""
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NimbusViewModel(nimbusConfig = nimbusConfig,
                    viewRequest = viewRequest,
                json = json ) as T
            }
        }
    }



    fun setModalHiddenState() {
        setNimbusViewModelModalState(NimbusViewModelModalState.RootState)
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

    fun getPageBy(url: String?): Page? {
        return  url?.let { pagesManager.getPageBy(url) }
    }

    fun getPageCount() = pagesManager.getPageCount()

    fun dispose() = viewModelScope.launch(CoroutineDispatcherLib.backgroundPool) {
        pagesManager.removeAllPages()
    }

    override fun onCleared() {
        super.onCleared()
        //Cannot call coroutines at this moment, should run everything on main thread
        pagesManager.removeAllPages()
    }

    private fun setNavigationState(nimbusViewModelNavigationState: NimbusViewModelNavigationState) {
        viewModelScope.launch(CoroutineDispatcherLib.backgroundPool) {
            _nimbusViewNavigationState.tryEmit(nimbusViewModelNavigationState)
        }
    }

    private fun setNimbusViewModelModalState(state: NimbusViewModelModalState) {
        viewModelScope.launch(CoroutineDispatcherLib.backgroundPool) {
            _nimbusViewModelModalState.tryEmit(state)
        }
    }

    private fun pushNavigation(page: Page, initialRequest: Boolean) {
        pagesManager.add(page)
        if (!initialRequest) {
            setNavigationState(NimbusViewModelNavigationState.Push(page.id))
        }
    }

    private fun popNavigation() = viewModelScope.launch(CoroutineDispatcherLib.backgroundPool) {
        pop()
    }

    private fun popNavigationTo(url: String) =
        viewModelScope.launch(CoroutineDispatcherLib.backgroundPool) {
            pagesManager.getPageBy(url)?.let {
                pagesManager.removePagesAfter(it)
                setNavigationState(NimbusViewModelNavigationState.PopTo(url))
            }
        }

    private fun doPushWithViewRequest(request: ViewRequest, initialRequest: Boolean = false) =
        viewModelScope.launch(CoroutineDispatcherLib.inputOutputPool) {
            val stateInstances = request.params?.map { ServerDrivenState(it.key, it.value) }
            val view = ServerDrivenView(
                nimbus = nimbusConfig,
                getNavigator = { serverDrivenNavigator },
                description = request.url,
                states = stateInstances,
            )
            val url = request.url
            val page = Page(
                id = url,
                view = view
            )
            pushNavigation(page = page, initialRequest = initialRequest)
            loadViewRequest(request, view, page)
        }

    private suspend fun loadViewRequest(
        request: ViewRequest,
        view: ServerDrivenView,
        page: Page,
    ) {
        try {
            page.setLoading()
            val tree = nimbusConfig.viewClient.fetch(request)
            tree.initialize(view)
            page.setContent(tree)
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            page.setError(
                throwable = e,
                retry = {
                    viewModelScope.launch(CoroutineDispatcherLib.inputOutputPool) {
                        loadViewRequest(
                            request = request,
                            view = view,
                            page = page
                        )
                    }
                }
            )
        }
    }

    private fun doPushWithJson(json: String) =
        viewModelScope.launch(CoroutineDispatcherLib.inputOutputPool) {
            val view = ServerDrivenView(
                nimbus = nimbusConfig,
                getNavigator = { serverDrivenNavigator },
                description = JSON
            )
            val url = JSON
            val page = Page(
                id = url,
                view = view
            )
            pushNavigation(page, true)
            loadJson(json, view, page)
        }

    private fun loadJson(
        json: String,
        view: ServerDrivenView,
        page: Page,
    ) {
        try {
            page.setLoading()
            val tree = nimbusConfig.nodeBuilder.buildFromJsonString(json)
            tree.initialize(view)
            page.setContent(tree)
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            page.setError(
                throwable = e,
                retry = {
                    viewModelScope.launch(CoroutineDispatcherLib.inputOutputPool) {
                        loadJson(
                            json = json,
                            view = view,
                            page = page
                        )
                    }
                }
            )
        }
    }
}

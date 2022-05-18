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
    private val nimbusConfig: NimbusConfig
) : ViewModel() {

    private val pages = ArrayList<Page>()
    var nimbusViewModelModalState: NimbusViewModelModalState by mutableStateOf(NimbusViewModelModalState.HiddenModalState)
        private set

    private var _nimbusViewNavigationState: MutableStateFlow<NimbusViewModelNavigationState> = MutableStateFlow(NimbusViewModelNavigationState.RootState)

    val nimbusViewNavigationState : StateFlow<NimbusViewModelNavigationState>
        get() = _nimbusViewNavigationState

    companion object {
        fun provideFactory(
            nimbusConfig: NimbusConfig
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
        if(pages.size <= 1) {
            return false
        }

        pages.removeLast()
        _nimbusViewNavigationState.value = NimbusViewModelNavigationState.Pop()

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

    private fun pushNavigation(page: Page, initialRequest: Boolean) = viewModelScope.launch {
        pages.add(page)
        if (!initialRequest) {
            _nimbusViewNavigationState.value = NimbusViewModelNavigationState.Push(page.id)
        }
    }

    private fun popNavigation() = viewModelScope.launch {
        pop()
    }

    private fun popNavigationTo(url: String) = viewModelScope.launch {
        val page = pages.firstOrNull {
            it.id == url
        }

        page?.let {
            page.removePagesAfter(pages)
            _nimbusViewNavigationState.value = NimbusViewModelNavigationState.PopTo(url)
        }
    }

    private fun doPushWithViewRequest(request: ViewRequest, initialRequest: Boolean = false) =
        viewModelScope.launch {
            val view = nimbusConfig.core.createView(serverDrivenNavigator)
            val url = if (initialRequest) VIEW_INITIAL_URL else request.url
            val page = Page(
                id = url, view = view
            )
            pushNavigation(page, initialRequest)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val tree = nimbusConfig.core.viewClient.fetch(request)
                    view.renderer.paint(tree)
                } catch (e: Throwable) {
                    page.content = NimbusPageState.PageStateOnError(
                        e
                    )
                }
            }

        }

    private fun doPushWithJson(json: String) = viewModelScope.launch {
        val view = nimbusConfig.core.createView(serverDrivenNavigator)
        val url = VIEW_INITIAL_URL
        val page = Page(
            id = url, view = view
        )
        pushNavigation(page, true)
        viewModelScope.launch(Dispatchers.IO) {
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

    override fun onCleared() {
        super.onCleared()
    }
}
package br.zup.com.nimbus.compose.core.ui.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import br.zup.com.nimbus.compose.NimbusConfig
import br.zup.com.nimbus.compose.SHOW_VIEW
import br.zup.com.nimbus.compose.VIEW_INITIAL_URL
import br.zup.com.nimbus.compose.VIEW_URL
import br.zup.com.nimbus.compose.core.ui.nimbusPopTo
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
import br.zup.com.nimbus.compose.model.removePagesAfter
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.network.ViewRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class NimbusViewModel(
    private val navController: NavHostController,
    private val nimbusConfig: NimbusConfig
) : ViewModel() {

    private val pages = ArrayList<Page>()

    companion object {
        fun provideFactory(
            navController: NavHostController,
            nimbusConfig: NimbusConfig
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NimbusViewModel(
                    nimbusConfig = nimbusConfig,
                    navController = navController
                ) as T
            }
        }
    }

    private val serverDrivenNavigator: ServerDrivenNavigator = object : ServerDrivenNavigator {
        override fun dismiss() {
            TODO("Not yet implemented")
        }

        override fun popTo(url: String) {
            popNavigationTo(url)
        }

        override fun present(request: ViewRequest) {
            TODO("Not yet implemented")
        }

        override fun pop() {
            popNavigation()
        }

        override fun push(request: ViewRequest) {
            doPushWithViewRequest(request)
        }
    }

    fun pop(): Boolean {
        val popped = navController.navigateUp()
        if (popped) {
            pages.removeLast()
        }
        return popped
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
            navController.navigate(
                "$SHOW_VIEW?$VIEW_URL=${
                    page.id
                }"
            )
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
            navController.nimbusPopTo(page.id)
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
}
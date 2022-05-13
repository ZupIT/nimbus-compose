package br.zup.com.nimbus.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import br.zup.com.nimbus.compose.core.navigator.NimbusServerDrivenNavigator
import br.zup.com.nimbus.compose.model.Page
import br.zup.com.nimbus.compose.core.ui.nimbusPopTo
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.ServerDrivenView
import kotlinx.coroutines.launch

internal class NimbusViewModel(
    private val navController: NavHostController,
    private val nimbusServerDrivenNavigator: NimbusServerDrivenNavigator
) : ViewModel(),
    NimbusServerDrivenNavigator.NavigatorListener {

    init {
        nimbusServerDrivenNavigator.registerNavigatorListener(this)
    }

    private val pages = ArrayList<Page>()

    companion object {
        fun provideFactory(
            navController: NavHostController,
            nimbusServerDrivenNavigator: NimbusServerDrivenNavigator
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NimbusViewModel(
                    nimbusServerDrivenNavigator = nimbusServerDrivenNavigator,
                    navController = navController
                ) as T
            }
        }
    }

    override fun onPush(
        request: ViewRequest,
        page: Page,
        view: ServerDrivenView,
        initialRequest: Boolean
    ) {
        viewModelScope.launch {
            push(page, initialRequest)
        }
    }

    override fun onPop() {
        viewModelScope.launch {
            pop()
        }
    }

    override fun onPopTo(url: String) {
        viewModelScope.launch {
            popTo(url)
        }
    }

    fun push(page: Page, initialRequest: Boolean) {
        pages.add(page)
        if (!initialRequest) {
            navController.navigate(
                "${SHOW_VIEW}?${VIEW_URL}=${
                    page.id
                }"
            )
        }
    }

    fun pop(): Boolean {
        val popped = navController.navigateUp()
        if (popped) {
            pages.removeLast()
        }
        return popped
    }

    fun popTo(url: String) {
        val page = pages.firstOrNull {
            it.id == url
        }

        page?.let {
            removePagesAfter(page)
            navController.nimbusPopTo(page.id)
        }
    }

    fun initFirstView(initialUrl: String) = viewModelScope.launch {
        nimbusServerDrivenNavigator.doPush(ViewRequest(url = initialUrl), initialRequest = true)
    }

    fun initFirstViewWithJson(json: String) = viewModelScope.launch {
        nimbusServerDrivenNavigator.doPushWithJson(json)
    }

    fun getPageBy(url: String): Page? {
        return pages.firstOrNull { it.id == url }
    }

    private fun removePagesAfter(page: Page) {
        val index = pages.indexOf(page)
        if (index < pages.lastIndex)
            pages.subList(index + 1, pages.size).clear()
    }

    override fun onCleared() {
        super.onCleared()
        pages.clear()
        nimbusServerDrivenNavigator.cleanUp()
    }
}
package br.zup.com.nimbus.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.ServerDrivenNode

class NimbusViewModel
    (
    private val navController: NavHostController,
    private val initialUrl: String,
    private val nimbusComposeNavigator: NimbusComposeNavigator
) : ViewModel(), NimbusComposeNavigator.NavigatorListener {

    init {
        nimbusComposeNavigator.registerNavigatorListener(this)
    }

    companion object {
        fun provideFactory(
            navController: NavHostController,
            initialUrl: String,
            nimbusComposeNavigator: NimbusComposeNavigator
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NimbusViewModel(
                    nimbusComposeNavigator = nimbusComposeNavigator, initialUrl = initialUrl,
                    navController = navController
                ) as T
            }
        }
    }

    fun initFirstView() {
        nimbusComposeNavigator.doPush(ViewRequest(url = initialUrl), initialRequest = true)
    }

    fun getPageBy(index: Int): Page? {
        return nimbusComposeNavigator.pages.elementAtOrNull(index)
    }

    override fun onPush(
        request: ViewRequest,
        pages: List<Page>,
        view: ServerDrivenView,
        initialRequest: Boolean
    ) {

        if (!initialRequest) {
            navController.navigate(
                "${ViewConstants.SHOW_VIEW}?${ViewConstants.VIEW_INDEX}=${nimbusComposeNavigator.pages.lastIndex}")
        }
    }

    override fun onPop() {
        navController.navigateUp()
    }
}
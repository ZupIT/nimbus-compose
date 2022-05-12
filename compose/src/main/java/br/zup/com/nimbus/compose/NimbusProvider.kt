package br.zup.com.nimbus.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.serverdriven.Nimbus
import br.zup.com.nimbus.compose.serverdriven.NimbusTheme.nimbusAppState
import br.zup.com.nimbus.compose.serverdriven.ProvideNimbusAppState
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.ServerDrivenView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NimbusComposeNavigator(
    private val nimbusCompose: Nimbus,
    private val coroutineScope: CoroutineScope) : ServerDrivenNavigator {
    private var navigatorListener: NavigatorListener? = null

    override fun dismiss() {
        TODO("Not yet implemented")
    }

    override fun popTo(url: String) {
        navigatorListener?.onPopTo(url)
    }

    override fun present(request: ViewRequest) {
        TODO("Not yet implemented")
    }

    override fun pop() {
        navigatorListener?.onPop()
    }

    override fun push(request: ViewRequest) {
        doPush(request)
    }

    fun cleanUp() {
        navigatorListener = null
    }

    fun doPush(request: ViewRequest, initialRequest: Boolean = false) {
        val view = nimbusCompose.core.createView(this)
        val url = if (initialRequest) VIEW_INITIAL_URL else request.url
        val page = Page(
            id = url, view = view)
        navigatorListener?.onPush(request, page, view, initialRequest)
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val tree = nimbusCompose.core.viewClient.fetch(request)
                view.renderer.paint(tree)
            } catch (e: Throwable) {
                page.content = NimbusPageState.PageStateOnError(
                    e
                )
            }
        }
    }

    fun registerNavigatorListener(navigatorListener: NavigatorListener) {
        this.navigatorListener = navigatorListener
    }

    interface NavigatorListener {
        fun onPush(request: ViewRequest, page: Page, view: ServerDrivenView, initialRequest: Boolean)
        fun onPop()
        fun onPopTo(url: String)

        //TODO fill in other methods
    }
}

//FIXME this should be passed as a parameter to the navigation
//var viewRequest: ViewRequest? = null


const val SHOW_VIEW = "showView"
const val VIEW_URL = "viewUrl"
const val VIEW_INITIAL_URL = "root"
const val SHOW_VIEW_DESTINATION = "${SHOW_VIEW}?${VIEW_URL}={${VIEW_URL}}"

@Composable
fun NimbusProvider(
    initialUrl: String,
    nimbus: Nimbus) {

    ProvideNimbusAppState(nimbusCompose = nimbus, initialUrl = initialUrl) {
        Scaffold { innerPaddingModifier ->
            NavHost(
                navController = nimbusAppState.navController,
                startDestination = SHOW_VIEW_DESTINATION,
                modifier = Modifier.padding(innerPaddingModifier)
            ) {
                composable(
                    route = SHOW_VIEW_DESTINATION,
                    arguments = listOf(navArgument(VIEW_URL) {
                        type = NavType.StringType
                        defaultValue = VIEW_INITIAL_URL
                    })
                ) { backStackEntry ->
                    val arguments = requireNotNull(backStackEntry.arguments)
                    val currentPageUrl = arguments.getString(VIEW_URL)
                    val nimbusViewModel = nimbusAppState.nimbusViewModel
                    val nimbusCompose = nimbusAppState.nimbusCompose
                    val currentPage = currentPageUrl?.let {
                        nimbusViewModel.getPageBy(
                            it
                        )
                    }
                    currentPage?.content?.let {
                        BackHandler(enabled = true) {
                            nimbusViewModel.onPop()
                        }
                        when(it) {
                            is NimbusPageState.PageStateOnLoading -> {
                                nimbusCompose.loadingView()
                            }
                            is NimbusPageState.PageStateOnError -> {
                                nimbusCompose.errorView(it.throwable)
                            }
                            is NimbusPageState.PageStateOnShowPage -> {
                                RenderTree(viewTree = it.serverDrivenNode)        
                            }
                        }
                    }
                }
            }
        }
    }
}

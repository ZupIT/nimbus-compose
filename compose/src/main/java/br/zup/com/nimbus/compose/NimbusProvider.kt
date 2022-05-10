package br.zup.com.nimbus.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.ViewConstants.SHOW_VIEW_DESTINATION
import br.zup.com.nimbus.compose.ViewConstants.VIEW_INDEX
import br.zup.com.nimbus.compose.serverdriven.Nimbus
import br.zup.com.nimbus.compose.serverdriven.NimbusTheme.nimbusAppState
import br.zup.com.nimbus.compose.serverdriven.ProvideNimbusAppState
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.network.NetworkError
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.ServerDrivenView
import com.zup.nimbus.core.tree.MalformedComponentError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NimbusComposeNavigator(
    private val nimbusCompose: Nimbus,
    private val coroutineScope: CoroutineScope) : ServerDrivenNavigator {
    private var navigatorListener: NavigatorListener? = null

    val pages = ArrayList<Page>()

    override fun dismiss() {
        TODO("Not yet implemented")
    }

    override fun popTo(url: String) {
        TODO("Not yet implemented")
    }

    override fun present(request: ViewRequest) {
        TODO("Not yet implemented")
    }

    override fun pop() {
        pages.removeLast()
        navigatorListener?.onPop()
    }

    override fun push(request: ViewRequest) {
        doPush(request)
    }

    fun doPush(request: ViewRequest, initialRequest: Boolean = false) {
        val view = nimbusCompose.core.createView(this)
        val page = Page(
            id = request.url, view = view)
        pages.add(page)

        coroutineScope.launch {
            try {
                navigatorListener?.onPush(request, pages, view, initialRequest)
                val tree = nimbusCompose.core.viewClient.fetch(request)
                view.renderer.paint(tree)
            } catch (e: NetworkError) {
                page.content = NimbusPageState.PageStateOnError(
                    e
                )
            } catch (e: MalformedComponentError) {
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
        fun onPush(request: ViewRequest, pages: List<Page>, view: ServerDrivenView, initialRequest: Boolean)
        fun onPop()

        //TODO fill in other methods
    }
}

//FIXME this should be passed as a parameter to the navigation
//var viewRequest: ViewRequest? = null
object ViewConstants {

    const val SHOW_VIEW = "showView"
    const val VIEW_INDEX = "viewIndex"

    const val SHOW_VIEW_DESTINATION = "${SHOW_VIEW}?${VIEW_INDEX}={${VIEW_INDEX}}"
}

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
                    arguments = listOf(navArgument(VIEW_INDEX) {
                        type = NavType.IntType
                        defaultValue = 0
                    })
                ) { backStackEntry ->
                    val arguments = requireNotNull(backStackEntry.arguments)
                    val currentPageIndex = arguments.getInt(VIEW_INDEX)
                    val currentPage = nimbusAppState.nimbusViewModel.getPageBy(currentPageIndex)
                    currentPage?.content?.let {
                        when(it) {
                            is NimbusPageState.PageStateOnLoading -> {
                                nimbusAppState.nimbusCompose.loadingView()
                            }
                            is NimbusPageState.PageStateOnError -> {
                                nimbusAppState.nimbusCompose.errorView(it.throwable)
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

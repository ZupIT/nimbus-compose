package br.zup.com.nimbus.compose.core.navigator

import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
import br.zup.com.nimbus.compose.VIEW_INITIAL_URL
import br.zup.com.nimbus.compose.NimbusConfig
import com.zup.nimbus.core.ServerDrivenNavigator
import com.zup.nimbus.core.network.ViewRequest
import com.zup.nimbus.core.render.ServerDrivenView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

internal class NimbusServerDrivenNavigator(
    private val nimbusConfig: NimbusConfig,
    private val coroutineScope: CoroutineScope
) : ServerDrivenNavigator {
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
        coroutineScope.coroutineContext.cancelChildren()
    }

    fun doPush(request: ViewRequest, initialRequest: Boolean = false) {

        coroutineScope.launch(Dispatchers.IO) {

                val view = nimbusConfig.core.createView(this@NimbusServerDrivenNavigator)
                val url = if (initialRequest) VIEW_INITIAL_URL else request.url
                val page = Page(
                    id = url, view = view)
                navigatorListener?.onPush(request, page, view, initialRequest)

                val tree = kotlin.runCatching { nimbusConfig.core.viewClient.fetch(request) }
                if(tree.isSuccess) {
                    tree.getOrNull()?.let { view.renderer.paint(it) }
                } else {
                    page.content = tree.exceptionOrNull()?.let {
                        NimbusPageState.PageStateOnError(
                            it
                        )
                    }
                }

        }
    }

    fun registerNavigatorListener(navigatorListener: NavigatorListener) {
        this.navigatorListener = navigatorListener
    }

    fun doPushWithJson(json: String) {
        coroutineScope.launch(Dispatchers.IO) {

            val view = nimbusConfig.core.createView(this@NimbusServerDrivenNavigator)
            val url = VIEW_INITIAL_URL
            val page = Page(
                id = url, view = view)
            navigatorListener?.onPush(ViewRequest(url = url), page, view, true)

            val tree = kotlin.runCatching { nimbusConfig.core.createNodeFromJson(json)}
            if(tree.isSuccess) {
                tree.getOrNull()?.let { view.renderer.paint(it) }
            } else {
                page.content = tree.exceptionOrNull()?.let {
                    NimbusPageState.PageStateOnError(
                        it
                    )
                }
            }
        }
    }

    interface NavigatorListener {
        fun onPush(request: ViewRequest, page: Page, view: ServerDrivenView, initialRequest: Boolean)
        fun onPop()
        fun onPopTo(url: String)

        //TODO fill in other methods
    }
}
package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.NimbusConfig
import br.zup.com.nimbus.compose.NimbusTheme.nimbusAppState
import br.zup.com.nimbus.compose.SHOW_VIEW_DESTINATION
import br.zup.com.nimbus.compose.VIEW_INITIAL_URL
import br.zup.com.nimbus.compose.VIEW_URL
import com.zup.nimbus.core.network.ViewRequest
import java.util.UUID

@Composable
internal fun NimbusNavHost(
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
    viewRequest: ViewRequest? = null,
    nimbusConfig: NimbusConfig = nimbusAppState.config,
    nimbusViewModel: NimbusViewModel = viewModel(
        //Creates a new viewmodel for each unique key
        key = viewModelKey,
        factory = NimbusViewModel.provideFactory(
            nimbusConfig = nimbusConfig
        )
    ),
    modalParentHelper: ModalTransitionDialogHelper = ModalTransitionDialogHelper(),
    nimbusNavHostHelper: NimbusNavHostHelper = NimbusNavHostHelper(),
    json: String = "",
    modifier: Modifier = Modifier,
) {

    NimbusNavigationEffect(nimbusViewModel, navController)

    NimbusDisposableEffect(
        onCreate = {
            initNavHost(nimbusViewModel, viewRequest, json, nimbusNavHostHelper)
        })

    NavHost(
        navController = navController,
        startDestination = SHOW_VIEW_DESTINATION,
        modifier = modifier
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
            val currentPage = currentPageUrl?.let {
                nimbusViewModel.getPageBy(
                    it
                )
            }
            currentPage?.let { page ->
                NimbusBackHandler(nimbusViewModel = nimbusViewModel)
                NimbusView(page = page)
                NimbusModalView(
                    nimbusViewModel = nimbusViewModel,
                    modalParentHelper = modalParentHelper
                )
            }
        }
    }
}

private fun initNavHost(
    nimbusViewModel: NimbusViewModel,
    viewRequest: ViewRequest?,
    json: String,
    navHostHelper: NimbusNavHostHelper) {
    navHostHelper.nimbusNavHostExecutor = object : NimbusNavHostHelper.NimbusNavHostExecutor {
        override fun pop(): Boolean = nimbusViewModel.pop()
        override fun dispose() {
            nimbusViewModel.dispose()
        }
    }

    if (viewRequest != null)
        nimbusViewModel.initFirstViewWithRequest(viewRequest = viewRequest)
    else
        nimbusViewModel.initFirstViewWithJson(json = json)

}

/**
 * This helper can be used to control some behaviour from outside the NimbusNavHost composable
 */
internal class NimbusNavHostHelper {

    var nimbusNavHostExecutor: NimbusNavHostExecutor? = null

    interface NimbusNavHostExecutor {
        fun pop(): Boolean
        fun dispose()
    }

    fun pop(): Boolean = nimbusNavHostExecutor?.pop() ?: false
    fun dispose() = nimbusNavHostExecutor?.dispose()
}

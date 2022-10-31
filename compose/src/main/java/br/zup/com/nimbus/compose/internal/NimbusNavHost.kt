package br.zup.com.nimbus.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.Nimbus
import br.zup.com.nimbus.compose.NimbusTheme.nimbus
import br.zup.com.nimbus.compose.ProvideNavigatorState
import br.zup.com.nimbus.compose.SHOW_VIEW_DESTINATION
import br.zup.com.nimbus.compose.VIEW_INITIAL_URL
import br.zup.com.nimbus.compose.VIEW_URL
import com.zup.nimbus.core.network.ViewRequest
import java.util.UUID

@Composable
internal fun NimbusNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
    viewRequest: ViewRequest? = null,
    nimbusConfig: Nimbus = nimbus,
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
) {

    CollectFlow(nimbusViewModel.nimbusViewNavigationState) { navigationState ->
        navigationState.handleNavigation(navController)
    }

    NimbusDisposableEffect(
        onCreate = {
            initNavHost(nimbusViewModel, viewRequest, json)
            configureNavHostHelper(nimbusNavHostHelper, nimbusViewModel)
        })

    ProvideNavigatorState(navHostHelper = nimbusNavHostHelper) {
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
                nimbusViewModel.getPageBy(
                    backStackEntry.getPageUrl()
                )?.let { page ->
                    NimbusBackHandler()
                    page.Compose()
                    NimbusModalView(
                        nimbusViewModel = nimbusViewModel,
                        modalParentHelper = modalParentHelper
                    )
                }
            }
        }
    }
}

private fun configureNavHostHelper(
    nimbusNavHostHelper: NimbusNavHostHelper,
    nimbusViewModel: NimbusViewModel,
) {
    nimbusNavHostHelper.nimbusNavHostExecutor = object : NimbusNavHostHelper.NimbusNavHostExecutor {
        override fun isFirstScreen(): Boolean = nimbusViewModel.getPageCount() == 1

        override fun pop(): Boolean = nimbusViewModel.pop()
    }
}

private fun initNavHost(
    nimbusViewModel: NimbusViewModel,
    viewRequest: ViewRequest?,
    json: String
) {

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
     fun isFirstScreen(): Boolean  = nimbusNavHostExecutor?.isFirstScreen() ?: false

     fun pop(): Boolean = nimbusNavHostExecutor?.pop() ?: false

    interface NimbusNavHostExecutor {
        fun isFirstScreen(): Boolean
        fun pop(): Boolean
    }
}

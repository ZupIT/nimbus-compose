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
    nimbusNavHostHelper: NimbusNavHostHelper = NimbusNavHostHelperImpl(),
    json: String = "",
) {

    NimbusNavigationEffect(nimbusViewModel, navController)

    NimbusDisposableEffect(
        onCreate = {
            initNavHost(nimbusViewModel, viewRequest, json)
        })

    ConfigureNavHostHelper(nimbusNavHostHelper, nimbusViewModel)

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
                val arguments = requireNotNull(backStackEntry.arguments)
                val currentPageUrl = arguments.getString(VIEW_URL)
                val currentPage = currentPageUrl?.let {
                    nimbusViewModel.getPageBy(
                        it
                    )
                }
                currentPage?.let { page ->
                    NimbusBackHandler()
                    NimbusView(page = page)
                    NimbusModalView(
                        nimbusViewModel = nimbusViewModel,
                        modalParentHelper = modalParentHelper
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigureNavHostHelper(
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

interface NimbusNavHostHelper {

    var nimbusNavHostExecutor: NimbusNavHostExecutor?

    interface NimbusNavHostExecutor {
        fun isFirstScreen(): Boolean
        fun pop(): Boolean
    }

    fun isFirstScreen(): Boolean
    fun pop(): Boolean
}

/**
 * This helper can be used to control some behaviour from outside the NimbusNavHost composable
 */
internal class NimbusNavHostHelperImpl : NimbusNavHostHelper {

    override var nimbusNavHostExecutor: NimbusNavHostHelper.NimbusNavHostExecutor? = null
    override fun isFirstScreen(): Boolean  = nimbusNavHostExecutor?.isFirstScreen() ?: false

    override fun pop(): Boolean = nimbusNavHostExecutor?.pop() ?: false
}

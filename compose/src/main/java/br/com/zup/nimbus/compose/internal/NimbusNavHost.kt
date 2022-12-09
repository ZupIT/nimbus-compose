package br.com.zup.nimbus.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.zup.nimbus.compose.JSON
import br.com.zup.nimbus.compose.Nimbus
import br.com.zup.nimbus.compose.ProvideNavigatorState
import br.com.zup.nimbus.compose.SHOW_VIEW_DESTINATION
import br.com.zup.nimbus.compose.VIEW_URL
import br.com.zup.nimbus.core.network.ViewRequest
import java.util.UUID

@Composable
internal fun NimbusNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
    viewRequest: ViewRequest? = null,
    nimbusConfig: Nimbus = Nimbus.instance,
    modalParentHelper: ModalTransitionDialogHelper = ModalTransitionDialogHelper(),
    nimbusNavHostHelper: NimbusNavHostHelper = NimbusNavHostHelper,
    json: String = "",
    nimbusViewModel: NimbusViewModel = viewModel(
        //Creates a new viewmodel for each unique key
        key = viewModelKey,
        factory = NimbusViewModel.provideFactory(nimbusConfig = nimbusConfig,
            viewRequest = viewRequest,
            json = json)
    ),
) {

    CollectFlow(nimbusViewModel.nimbusViewNavigationState) { navigationState ->
        navigationState.handleNavigation(navController)
    }

    val nimbusViewModelModalState: NimbusViewModelModalState by
    nimbusViewModel.nimbusViewModelModalState.collectAsState()

    NimbusDisposableEffect(
        onCreate = {
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
                    defaultValue = viewRequest?.url ?: JSON
                })
            ) { backStackEntry ->
                NimbusBackHandler(onDismiss =
                {
                    modalParentHelper.triggerAnimatedClose()
                })

                nimbusViewModelModalState.HandleModalState(
                    onDismiss = {
                        nimbusViewModel.setModalHiddenState()
                    },
                    onHideModal = {
                        modalParentHelper.triggerAnimatedClose()
                    }
                )

                val page = nimbusViewModel.getPageBy(
                    backStackEntry.getPageUrl()
                )

                val pageRemember by remember(page?.id) { mutableStateOf(page) }
                pageRemember?.Compose()
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

/**
 * This helper can be used to control some behaviour from outside the NimbusNavHost composable
 */
object NimbusNavHostHelper {

    var nimbusNavHostExecutor: NimbusNavHostExecutor? = null
    fun isFirstScreen(): Boolean = nimbusNavHostExecutor?.isFirstScreen() ?: false

    fun pop(): Boolean = nimbusNavHostExecutor?.pop() ?: false

    interface NimbusNavHostExecutor {
        fun isFirstScreen(): Boolean
        fun pop(): Boolean
    }
}

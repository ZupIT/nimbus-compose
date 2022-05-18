package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.NimbusTheme
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
    nimbusViewModel: NimbusViewModel = viewModel(
        //Creates a new viewmodel for each unique key
        key = viewModelKey,
        factory = NimbusViewModel.provideFactory(
            navController = navController,
            nimbusConfig = NimbusTheme.nimbusAppState.config
        )
    ),
    modalParentHelper: ModalTransitionDialogHelper = ModalTransitionDialogHelper(),
    nimbusNavHostHelper: NimbusNavHostHelper = NimbusNavHostHelper(),
    json: String = "",
    modifier: Modifier = Modifier,
) {
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
                NimbusView(
                    page = page,
                    nimbusViewModel = nimbusViewModel)
                val modalTransitionDialogHelper = ModalTransitionDialogHelper()
                val navHostHelper = NimbusNavHostHelper()
                if (nimbusViewModel.nimbusViewModelModalState is NimbusViewModelModalState.OnShowModalModalState) {
                    val showModalState = (nimbusViewModel.nimbusViewModelModalState as? NimbusViewModelModalState.OnShowModalModalState)
                    ModalTransitionDialog(
                        modalTransitionDialogHelper = modalTransitionDialogHelper,
                        onDismissRequest = {
                            nimbusViewModel.nimbusViewModelModalState =
                            NimbusViewModelModalState.HiddenModalState
                        },
                        onCanDismissRequest = {
                            //Can dismiss the modal if we cannot pop more pages from navigation host
                            !navHostHelper.pop()
                        }
                    ) {
                        NimbusNavHost(
                            modalParentHelper = modalTransitionDialogHelper,
                            nimbusNavHostHelper = navHostHelper,
                            viewRequest = showModalState?.viewRequest,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(Color.White)
                                .padding(16.dp),
                        )
                    }
                } else if (nimbusViewModel.nimbusViewModelModalState is NimbusViewModelModalState.OnHideModalState) {
                    modalParentHelper.triggerAnimatedClose()
                }
            }
        }
    }
}

internal class NimbusNavHostHelper {

    var nimbusNavHostExecutor: NimbusNavHostExecutor? = null

    interface NimbusNavHostExecutor {
        fun pop(): Boolean
    }

    fun pop(): Boolean = nimbusNavHostExecutor?.pop() ?: false

    fun clear() {
        nimbusNavHostExecutor = null
    }
}

private fun initNavHost(
    nimbusViewModel: NimbusViewModel,
    viewRequest: ViewRequest?,
    json: String,
    navHostHelper: NimbusNavHostHelper,
) {
    navHostHelper.nimbusNavHostExecutor = object : NimbusNavHostHelper.NimbusNavHostExecutor {
        override fun pop(): Boolean = nimbusViewModel.pop()
    }

    if (viewRequest != null)
        nimbusViewModel.initFirstViewWithRequest(viewRequest = viewRequest)
    else
        nimbusViewModel.initFirstViewWithJson(json = json)
}
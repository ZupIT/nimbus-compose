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
    triggerDismissModal: () -> Unit = {},
    nimbusViewModel: NimbusViewModel = viewModel(
        //Creates a new viewmodel for each unique key
        key = viewModelKey,
        factory = NimbusViewModel.provideFactory(
            navController = navController,
            nimbusConfig = NimbusTheme.nimbusAppState.config
        )
    ),
    viewModelReference: MutableList<NimbusViewModel> = mutableListOf(),
    json: String = "",
    modifier: Modifier = Modifier,
) {
    viewModelReference.add(nimbusViewModel)
    NimbusDisposableEffect(
        onCreate = {
            initView(nimbusViewModel, triggerDismissModal, viewRequest, json)
        },
         onDispose = {
            viewModelReference.clear()
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

                nimbusViewModel.showModalTransitionDialog?.let { viewRequest ->
                val viewModelReference = mutableListOf<NimbusViewModel>()
                    ModalTransitionDialog(
                        onDismissRequest = { nimbusViewModel.showModalTransitionDialog = null },
                        onCanDismissRequest = {
                            //Can dismiss the modal if we cannot pop more pages from navigation host
                            viewModelReference.first().pop().not()
                        }
                    ) {
                        NimbusNavHost(
                            viewModelReference = viewModelReference,
                            viewRequest = viewRequest.copy(),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(Color.White)
                                .padding(16.dp),
                            triggerDismissModal = it::triggerAnimatedClose)
                    }
                }
            }
        }
    }
}

private fun initView(
    nimbusViewModel: NimbusViewModel,
    triggerDismissModal: () -> Unit,
    viewRequest: ViewRequest?,
    json: String
) {
    nimbusViewModel.triggerDismissModal = triggerDismissModal
    if (viewRequest != null)
        nimbusViewModel.initFirstViewWithRequest(viewRequest = viewRequest)
    else
        nimbusViewModel.initFirstViewWithJson(json = json)
}
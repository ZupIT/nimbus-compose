package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import java.util.UUID

@Composable
internal fun NimbusNavHost(
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
    viewRequest: ViewRequest? = null,
    onDismiss: () -> Unit = {},
    json: String = "",
    modifier: Modifier = Modifier,
) {
    val nimbusConfig = NimbusTheme.nimbusAppState.config

    val nimbusViewModel: NimbusViewModel = viewModel(
        //Creates a new viewmodel for each unique key
        key = viewModelKey,
        factory = NimbusViewModel.provideFactory(
            navController = navController,
            nimbusConfig = nimbusConfig
        )
    )

    NimbusDisposableEffect(
        onCreate = {
            initView(nimbusViewModel, onDismiss, viewRequest, json)
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
                    ModalTransitionDialog(
                        onBack = {
                            print("back")
                        },
                        onDismissRequest = {
                        nimbusViewModel.showModalTransitionDialog = null
                    }) {
                        NimbusNavHost(
                            viewRequest = viewRequest.copy(),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(Color.White)
                                .padding(16.dp),
                            onDismiss = it::triggerAnimatedClose)
                    }
                }
            }
        }
    }


}

private fun initView(
    nimbusViewModel: NimbusViewModel,
    onDismiss: () -> Unit,
    viewRequest: ViewRequest?,
    json: String
) {
    nimbusViewModel.triggerDismissModal = onDismiss
    if (viewRequest != null)
        nimbusViewModel.initFirstViewWithRequest(viewRequest = viewRequest)
    else
        nimbusViewModel.initFirstViewWithJson(json = json)
}
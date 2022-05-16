package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.NimbusTheme
import br.zup.com.nimbus.compose.SHOW_VIEW_DESTINATION
import br.zup.com.nimbus.compose.VIEW_INITIAL_URL
import br.zup.com.nimbus.compose.VIEW_URL
import com.zup.nimbus.core.network.ViewRequest

@Composable
internal fun NimbusNavHost(
    viewModelKey: String,
    navController: NavHostController,
    viewRequest: ViewRequest? = null,
    json: String = "",
    modifier: Modifier
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

    if (viewRequest != null)
        nimbusViewModel.initFirstViewWithRequest(viewRequest = viewRequest)
    else
        nimbusViewModel.initFirstViewWithJson(json = json)

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
                NimbusView(page, nimbusViewModel)
            }
        }
    }
}
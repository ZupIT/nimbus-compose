package br.zup.com.nimbus.compose

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.core.navigator.NimbusServerDrivenNavigator
import br.zup.com.nimbus.compose.core.ui.NimbusView
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.NimbusTheme.nimbusAppState
import java.util.UUID


const val SHOW_VIEW = "showView"
const val VIEW_URL = "viewUrl"
const val VIEW_INITIAL_URL = "root"
const val SHOW_VIEW_DESTINATION = "${SHOW_VIEW}?${VIEW_URL}={${VIEW_URL}}"

@Composable
fun NimbusNavigator(
    initialUrl: String,
    navController: NavHostController = rememberNavController(),
    viewModelKey: String? = UUID.randomUUID().toString(),
    modifier: Modifier = Modifier
) {
    val nimbusConfig = nimbusAppState.nimbusConfig

    val nimbusViewModel: NimbusViewModel = viewModel(
        //Creates a new viewmodel for each unique key
        key = viewModelKey,
        factory = NimbusViewModel.provideFactory(
            navController = navController,
            nimbusServerDrivenNavigator = NimbusServerDrivenNavigator(
                nimbusConfig = nimbusConfig,
                coroutineScope = nimbusAppState.coroutineScope
            )
        )
    )

    nimbusViewModel.initFirstView(initialUrl = initialUrl)

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

            currentPage?.content?.let {
                NimbusBackHandler(nimbusViewModel)
                when (it) {
                    is NimbusPageState.PageStateOnLoading -> {
                        nimbusConfig.loadingView()
                    }
                    is NimbusPageState.PageStateOnError -> {
                        nimbusConfig.errorView(it.throwable)
                    }
                    is NimbusPageState.PageStateOnShowPage -> {
                        NimbusView(viewTree = it.serverDrivenNode)
                    }
                }
            }
        }
    }
}

@Composable
private fun NimbusBackHandler(nimbusViewModel: NimbusViewModel) {
    val activity = LocalContext.current as Activity
    BackHandler(enabled = true) {
        if (!nimbusViewModel.onPop()) {
            activity.finish()
        }
    }
}

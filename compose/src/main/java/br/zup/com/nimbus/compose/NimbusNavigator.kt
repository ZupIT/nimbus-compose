package br.zup.com.nimbus.compose

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.zup.com.nimbus.compose.NimbusTheme.nimbusAppState
import br.zup.com.nimbus.compose.core.navigator.NimbusServerDrivenNavigator
import br.zup.com.nimbus.compose.core.ui.NimbusServerDrivenView
import br.zup.com.nimbus.compose.model.NimbusPageState
import br.zup.com.nimbus.compose.model.Page
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

            currentPage?.let { page ->
                NimbusView(page, nimbusViewModel)
            }
        }
    }
}

@Composable
internal fun NimbusView(
    page: Page,
    nimbusViewModel: NimbusViewModel,
    onStart: () -> Unit = {},
    onCreate: () -> Unit = {},
    onDestroy: () -> Unit = {},
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when(event) {
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_CREATE -> onCreate()
                Lifecycle.Event.ON_DESTROY -> onDestroy()
            }

        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            page.view.destroy()
        }
    }

    page.content?.let {
        NimbusBackHandler(nimbusViewModel)
        when (it) {
            is NimbusPageState.PageStateOnLoading -> {
                nimbusAppState.nimbusConfig.loadingView()
            }
            is NimbusPageState.PageStateOnError -> {
                nimbusAppState.nimbusConfig.errorView(it.throwable)
            }
            is NimbusPageState.PageStateOnShowPage -> {
                NimbusServerDrivenView(viewTree = it.serverDrivenNode)
            }
        }
    }
}

@Composable
private fun NimbusBackHandler(nimbusViewModel: NimbusViewModel) {
    val activity = LocalContext.current as Activity
    BackHandler(enabled = true) {
        if (!nimbusViewModel.pop()) {
            activity.finish()
        }
    }
}

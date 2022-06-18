package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import br.zup.com.nimbus.compose.CoroutineDispatcherLib
import br.zup.com.nimbus.compose.SHOW_VIEW
import br.zup.com.nimbus.compose.VIEW_URL
import br.zup.com.nimbus.compose.core.ui.nimbusPopTo
import kotlinx.coroutines.withContext

@Composable
internal fun NimbusNavigationEffect(
    nimbusViewModel: NimbusViewModel,
    navController: NavHostController,
) {
    LaunchedEffect(key1 = Unit) {
        withContext(CoroutineDispatcherLib.backgroundPool) {
            nimbusViewModel.nimbusViewNavigationState.collect { navigationState ->
                withContext(CoroutineDispatcherLib.mainThread) {
                    when (navigationState) {
                        is NimbusViewModelNavigationState.Pop -> {
                            navController.navigateUp()
                        }
                        is NimbusViewModelNavigationState.PopTo -> {
                            navController.nimbusPopTo(navigationState.url)
                        }
                        is NimbusViewModelNavigationState.Push -> {
                            navController.navigate(
                                "$SHOW_VIEW?$VIEW_URL=${
                                    navigationState.url
                                }"
                            )
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }
}

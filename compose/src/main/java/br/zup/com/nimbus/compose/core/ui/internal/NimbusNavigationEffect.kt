package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import br.zup.com.nimbus.compose.SHOW_VIEW
import br.zup.com.nimbus.compose.VIEW_URL
import br.zup.com.nimbus.compose.core.ui.nimbusPopTo

@Composable
internal fun NimbusNavigationEffect(
    nimbusViewModel: NimbusViewModel,
    navController: NavHostController,
) {
    CollectStateFlow(nimbusViewModel.nimbusViewNavigationState) { navigationState ->
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

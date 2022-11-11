package br.zup.com.nimbus.compose.internal

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import br.zup.com.nimbus.compose.SHOW_VIEW_DESTINATION_PARAM
import br.zup.com.nimbus.compose.VIEW_URL
import br.zup.com.nimbus.compose.model.Page

internal fun NavBackStackEntry.getPageUrl() : String? {
    val arguments = requireNotNull(this.arguments)
    return arguments.getString(VIEW_URL)
}

internal fun NimbusViewModelNavigationState.handleNavigation(
    navController: NavHostController,
) {
    when (this) {
        is NimbusViewModelNavigationState.Pop -> {
            navController.navigateUp()
        }
        is NimbusViewModelNavigationState.PopTo -> {
            navController.nimbusPopTo(this.url)
        }
        is NimbusViewModelNavigationState.Push -> {
            navController.navigate(
                "$SHOW_VIEW_DESTINATION_PARAM=${
                    this.url
                }"
            )
        }
        else -> {
        }
    }
}

internal fun Page.removePagesAfter(pages: MutableList<Page>) {
    val index = pages.indexOf(this)
    if (index < pages.lastIndex)
        pages.subList(index + 1, pages.size).clear()
}

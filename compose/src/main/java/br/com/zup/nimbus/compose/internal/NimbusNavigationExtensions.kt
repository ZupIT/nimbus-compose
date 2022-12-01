package br.com.zup.nimbus.compose.internal

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import br.com.zup.nimbus.compose.ErrorHandler
import br.com.zup.nimbus.compose.LoadingHandler
import br.com.zup.nimbus.compose.SHOW_VIEW_DESTINATION_PARAM
import br.com.zup.nimbus.compose.VIEW_URL
import br.com.zup.nimbus.compose.model.NimbusPageState
import br.com.zup.nimbus.compose.model.Page

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

@Composable
internal fun NimbusViewModelModalState.HandleModalState(
    onDismiss: () -> Unit,
    onHideModal: () -> Unit,
) {
    if (this is NimbusViewModelModalState.OnShowModalModalState) {
        NimbusModalView(
            viewRequest = this.viewRequest,
            onDismiss = onDismiss
        )
    } else if (this is NimbusViewModelModalState.OnHideModalState) {
        onHideModal()
    }
}

@Composable
internal fun NimbusPageState.HandleNimbusPageState(
    loadingView: LoadingHandler,
    errorView: ErrorHandler,
) {
    when (this) {
        is NimbusPageState.PageStateOnLoading -> {
            loadingView()
        }
        is NimbusPageState.PageStateOnError -> {
            errorView(this.throwable,
                this.retry)
        }
        is NimbusPageState.PageStateOnShowPage -> {
            RenderedNode(flow = NodeFlow(this.node))
        }
    }
}

internal fun Page.removePagesAfter(pages: MutableList<Page>) {
    val index = pages.indexOf(this)
    if (index < pages.lastIndex)
        pages.subList(index + 1, pages.size).clear()
}

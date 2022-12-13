/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.compose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

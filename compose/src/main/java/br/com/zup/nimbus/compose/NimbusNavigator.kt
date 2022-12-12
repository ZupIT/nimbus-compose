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

package br.com.zup.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.com.zup.nimbus.compose.internal.NimbusNavHost
import br.com.zup.nimbus.core.network.ViewRequest
import java.util.UUID

const val SHOW_VIEW = "showView"
const val VIEW_URL = "viewUrl"
const val JSON = "json"
const val SHOW_VIEW_DESTINATION_PARAM = "$SHOW_VIEW?$VIEW_URL"
const val SHOW_VIEW_DESTINATION = "$SHOW_VIEW_DESTINATION_PARAM={$VIEW_URL}"

@Composable
fun NimbusNavigator(
    json: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
) {
    NimbusNavHost(
        viewModelKey = viewModelKey,
        navController = navController,
        json = json,
        modifier = modifier,
    )
}

@Composable
fun NimbusNavigator(
    viewRequest: ViewRequest,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
) {
    NimbusNavHost(
        viewModelKey = viewModelKey,
        navController = navController,
        viewRequest = viewRequest,
        modifier = modifier,
    )
}

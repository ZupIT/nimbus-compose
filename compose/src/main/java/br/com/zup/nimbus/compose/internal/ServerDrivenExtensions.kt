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

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import br.com.zup.nimbus.compose.SHOW_VIEW
import br.com.zup.nimbus.compose.VIEW_URL

internal fun NavHostController.nimbusPopTo(url: String) {
    if (removeFromStackMatchingArg(
            navController = this,
            arg = VIEW_URL,
            argValue = url)
    ) {
        this.navigate("$SHOW_VIEW?$VIEW_URL=${url}")
    }
}

private fun removeFromStackMatchingArg(
    navController: NavHostController,
    arg: String,
    argValue: Any?): Boolean {
    var elementFound = false
    val removeList = mutableListOf<NavBackStackEntry>()
    for (item in navController.backQueue.reversed()) {
        if (item.destination.route == navController.graph.startDestinationRoute) {
            if (item.arguments?.get(
                    arg
                ) == argValue
            ) {
                removeList.add(item)
                elementFound = true
                break
            } else {
                removeList.add(item)
            }
        }
    }

    if (elementFound) {
        navController.backQueue.removeAll(removeList)
    }
    return elementFound
}

package br.zup.com.nimbus.compose.internal

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import br.zup.com.nimbus.compose.SHOW_VIEW
import br.zup.com.nimbus.compose.VIEW_URL

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

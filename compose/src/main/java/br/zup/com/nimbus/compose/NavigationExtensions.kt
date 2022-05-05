package br.zup.com.nimbus.compose

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.squareup.moshi.Moshi

inline fun <reified T> NavController.navigate(
    route: String,
    data: Pair<String, T>,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
) {
    val count = route
        .split("{${data.first}}")
        .size
        .dec()

    if (count != 1) {
        throw IllegalArgumentException()
    }

    val out = Moshi.Builder()
        .build()
        .adapter(T::class.java)
        .toJson(data.second)
    val newRoute = route.replace(
        oldValue = "{${data.first}}",
        newValue = Uri.encode(out),
    )

    navigate(
        request = NavDeepLinkRequest.Builder
            .fromUri(NavDestination.createRoute(route = newRoute).toUri())
            .build(),
        navOptions = navOptions,
        navigatorExtras = navigatorExtras,
    )
}

inline fun <reified T> NavBackStackEntry.getData(key: String): T? {
    val data = arguments?.getString(key)

    return when {
        data != null -> Moshi.Builder()
            .build()
            .adapter(T::class.java)
            .fromJson(data)
        else -> null
    }
}

@Composable
inline fun <reified T> NavBackStackEntry.rememberGetData(key: String): T? {
    return remember { getData<T>(key) }
}
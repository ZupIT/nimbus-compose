package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.zup.com.nimbus.compose.core.ui.internal.NimbusNavHost
import com.zup.nimbus.core.network.ViewRequest
import java.util.UUID

const val SHOW_VIEW = "showView"
const val VIEW_URL = "viewUrl"
const val VIEW_INITIAL_URL = "root"
const val SHOW_VIEW_DESTINATION = "${SHOW_VIEW}?${VIEW_URL}={${VIEW_URL}}"

@Composable
fun NimbusNavigator(
    json: String,
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
    modifier: Modifier = Modifier,
) {
    NimbusNavHost(
        viewModelKey = viewModelKey,
        navController = navController,
        json = json,
        modifier = modifier
    )
}

@Composable
fun NimbusNavigator(
    viewRequest: ViewRequest,
    navController: NavHostController = rememberNavController(),
    viewModelKey: String = UUID.randomUUID().toString(),
    modifier: Modifier = Modifier
) {
    NimbusNavHost(
        viewModelKey = viewModelKey,
        navController = navController,
        viewRequest = viewRequest,
        modifier = modifier,
    )
}
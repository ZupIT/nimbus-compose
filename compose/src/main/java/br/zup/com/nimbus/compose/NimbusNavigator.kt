package br.zup.com.nimbus.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.zup.com.nimbus.compose.internal.NimbusNavHost
import com.zup.nimbus.core.network.ViewRequest
import java.util.UUID

const val SHOW_VIEW = "showView"
const val VIEW_URL = "viewUrl"
const val VIEW_INITIAL_URL = "root"
const val VIEW_JSON_DESCRIPTION = "json"
const val SHOW_VIEW_DESTINATION_PARAM = "${SHOW_VIEW}?${VIEW_URL}"
const val SHOW_VIEW_DESTINATION = "$SHOW_VIEW_DESTINATION_PARAM={${VIEW_URL}}"

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

package br.zup.com.nimbus.compose.internal

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import br.zup.com.nimbus.compose.NimbusTheme

@Composable
internal fun NimbusBackHandler(onDismiss: ()-> Unit = {}) {
    val activity = LocalContext.current as? Activity
    val navHostHelper = NimbusTheme.nimbusNavigatorState.navHostHelper
    BackHandler(enabled = true) {
        if (!navHostHelper.pop()) {
            activity?.finish()
            onDismiss()
        }
    }
}

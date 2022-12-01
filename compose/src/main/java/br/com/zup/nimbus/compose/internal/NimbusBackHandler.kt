package br.com.zup.nimbus.compose.internal

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import br.com.zup.nimbus.compose.Nimbus

@Composable
internal fun NimbusBackHandler(onDismiss: ()-> Unit = {}) {
    val activity = LocalContext.current as? Activity
    val navHostHelper = Nimbus.navigatorInstance.navHostHelper
    BackHandler(enabled = true) {
        if (!navHostHelper.pop()) {
            activity?.finish()
            onDismiss()
        }
    }
}

package br.zup.com.nimbus.compose.core.ui.internal

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun NimbusBackHandler(nimbusViewModel: NimbusViewModel) {
    val activity = LocalContext.current as Activity
    BackHandler(enabled = true) {
        if (!nimbusViewModel.pop()) {
            activity.finish()
        }
    }
}

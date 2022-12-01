package br.com.zup.nimbus.compose.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.zup.nimbus.core.network.ViewRequest

@Composable
internal fun NimbusModalView(
    viewRequest: ViewRequest,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
) {

        ModalTransitionDialog(
            onDismissRequest = onDismiss,
        ) {
            NimbusNavHost(
                modalParentHelper = it,
                viewRequest = viewRequest,
                modifier = modifier
            )
        }

}

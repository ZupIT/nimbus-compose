package br.zup.com.nimbus.compose.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun NimbusModalView(
    nimbusViewModel: NimbusViewModel,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(Color.White),
) {
    val nimbusViewModelModalState: NimbusViewModelModalState by
        nimbusViewModel.nimbusViewModelModalState.collectAsState()

    if (nimbusViewModelModalState is NimbusViewModelModalState.OnShowModalModalState) {
        val showModalState =
            (nimbusViewModelModalState as? NimbusViewModelModalState.OnShowModalModalState)
        ModalTransitionDialog(
            onDismissRequest = {
                nimbusViewModel.setModalHiddenState()
            },
        ) {
            NimbusNavHost(
                modalParentHelper = it,
                viewRequest = showModalState?.viewRequest,
                modifier = modifier
            )
        }
    }

}

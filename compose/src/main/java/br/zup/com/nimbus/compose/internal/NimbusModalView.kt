package br.zup.com.nimbus.compose.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun NimbusModalView(
    nimbusViewModel: NimbusViewModel,
    modifier: Modifier = Modifier.fillMaxSize()
        .background(Color.White),
) {
    var nimbusViewModelModalState: NimbusViewModelModalState by remember {
        mutableStateOf(NimbusViewModelModalState.HiddenModalState)
    }
    val navHostHelper = NimbusNavHostHelper()
    val modalParentHelper = ModalTransitionDialogHelper()
    if (nimbusViewModelModalState is NimbusViewModelModalState.OnShowModalModalState) {
        val showModalState =
            (nimbusViewModelModalState as? NimbusViewModelModalState.OnShowModalModalState)
        ModalTransitionDialog(
            modalTransitionDialogHelper = modalParentHelper,
            onDismissRequest = {
                nimbusViewModel.setModalHiddenState()
            },
        ) {
                NimbusNavHost(
                    modalParentHelper = it,
                    nimbusNavHostHelper = navHostHelper,
                    viewRequest = showModalState?.viewRequest,
                    modifier = modifier
                )
        }
    }

    CollectFlow(nimbusViewModel.nimbusViewModelModalState) {
        if (it != nimbusViewModelModalState)
            nimbusViewModelModalState = it
    }
}

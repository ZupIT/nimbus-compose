package br.zup.com.nimbus.compose.core.ui.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun NimbusModalView(
    nimbusViewModel: NimbusViewModel,
    modalParentHelper: ModalTransitionDialogHelper,
) {
    val modalTransitionDialogHelper = ModalTransitionDialogHelper()
    val navHostHelper = NimbusNavHostHelper()
    if (nimbusViewModel.nimbusViewModelModalState is NimbusViewModelModalState.OnShowModalModalState) {
        val showModalState =
            (nimbusViewModel.nimbusViewModelModalState as? NimbusViewModelModalState.OnShowModalModalState)
        ModalTransitionDialog(
            modalTransitionDialogHelper = modalTransitionDialogHelper,
            onDismissRequest = {
                nimbusViewModel.setModalHiddenState()
            },
            onCanDismissRequest = {
                //Can dismiss the modal if we cannot pop more pages from navigation host
                !navHostHelper.pop()
            }
        ) {
            NimbusNavHost(
                modalParentHelper = modalTransitionDialogHelper,
                nimbusNavHostHelper = navHostHelper,
                viewRequest = showModalState?.viewRequest,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color.White)
                    .padding(16.dp),
            )
        }
    } else if (nimbusViewModel.nimbusViewModelModalState is NimbusViewModelModalState.OnHideModalState) {
        modalParentHelper.triggerAnimatedClose()
    }
}
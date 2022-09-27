package br.zup.com.nimbus.compose.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun NimbusModalView(
    nimbusViewModel: NimbusViewModel,
    modalParentHelper: ModalTransitionDialogHelper,
) {
    var nimbusViewModelModalState: NimbusViewModelModalState by remember {
        mutableStateOf(NimbusViewModelModalState.HiddenModalState)
    }
    val modalTransitionDialogHelper = ModalTransitionDialogHelper()
    val navHostHelper = NimbusNavHostHelperImpl()

    if (nimbusViewModelModalState is NimbusViewModelModalState.OnShowModalModalState) {
        val showModalState =
            (nimbusViewModelModalState as? NimbusViewModelModalState.OnShowModalModalState)
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
    } else if (nimbusViewModelModalState is NimbusViewModelModalState.OnHideModalState) {
        modalParentHelper.triggerAnimatedClose()
    }

    CollectSharedFlow(nimbusViewModel.nimbusViewModelModalState) {
        nimbusViewModelModalState = it
    }
}

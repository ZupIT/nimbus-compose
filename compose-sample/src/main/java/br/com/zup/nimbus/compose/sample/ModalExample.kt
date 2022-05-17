package br.com.zup.nimbus.compose.sample

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.zup.com.nimbus.compose.core.ui.internal.ModalTransitionDialog

@Composable
fun MainScreen() {

    var showModalTransitionDialog by remember { mutableStateOf(false) }

    MainContent(
        onDismissRequest = { showModalTransitionDialog = false },
        showModalTransitionDialog = showModalTransitionDialog,
        onShowDialog = { showModalTransitionDialog = true }
    )
}

@Composable
fun MainContent(
    onShowDialog: () -> Unit,
    onDismissRequest: () -> Unit,
    showModalTransitionDialog: Boolean
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            onClick = onShowDialog
        ) {
            Text(text = "Show dialog")
        }
    }

    if (showModalTransitionDialog) {
        SampleModalTransitionDialog(onDismissRequest)
    }
}

@Composable
fun SampleModalTransitionDialog(
    onDismissRequest: () -> Unit = {}
) {
    ModalTransitionDialog(onDismissRequest = onDismissRequest) { modalTransitionDialogHelper ->
        ModalContent(onClose = modalTransitionDialogHelper::triggerAnimatedClose)
    }
}

@Composable
fun ModalContent(onClose: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        IconButton(
            modifier = Modifier.align(Alignment.End),
            onClick = onClose,
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close"
            )
        }

        Spacer(modifier = Modifier.size(32.dp))

        Text(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "Modal Transition Dialog"
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center),
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.black)),
                onClick = onClose
            ) {
                Text(text = "Close it", color = Color.White)
            }
        }
    }
}
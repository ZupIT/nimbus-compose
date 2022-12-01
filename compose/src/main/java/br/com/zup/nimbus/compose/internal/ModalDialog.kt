package br.com.zup.nimbus.compose.internal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Figured out by trial and error
 */
private const val DIALOG_BUILD_TIME = 300L

/**
 * [Dialog] which uses a modal transition to animate in and out its content.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ModalTransitionDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(Color.White),
    dismissOnBackPress: Boolean = true,
    modalTransitionDialogHelper: ModalTransitionDialogHelper = ModalTransitionDialogHelper(),
    content: @Composable (ModalTransitionDialogHelper) -> Unit
) {

    val onCloseFlow: MutableStateFlow<Boolean> = remember { MutableStateFlow(false) }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val animateContentBackTrigger = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        launch {
            delay(DIALOG_BUILD_TIME)
            animateContentBackTrigger.value = true
        }
        launch {
            onCloseFlow.collectLatest { shouldClose ->
                if(shouldClose)
                    startDismissWithExitAnimation(animateContentBackTrigger, onDismissRequest)
            }
        }
    }

    Dialog(
        onDismissRequest = {
            coroutineScope.launch {
                startDismissWithExitAnimation(animateContentBackTrigger, onDismissRequest)
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = false)
    ) {
        AnimatedModalBottomSheetTransition(
            modifier = modifier,
            visible = animateContentBackTrigger.value) {
            modalTransitionDialogHelper.onCloseFlow = onCloseFlow
            modalTransitionDialogHelper.coroutineScope = coroutineScope
            content(modalTransitionDialogHelper)
        }
    }
}

private suspend fun startDismissWithExitAnimation(
    animateContentBackTrigger: MutableState<Boolean>,
    onDismissRequest: () -> Unit
) {
    animateContentBackTrigger.value = false
    delay(ANIMATION_TIME)
    onDismissRequest()
}

/**
 * Helper class that can be used inside the content scope from
 * composables that implement the [ModalTransitionDialog] to hide
 * the [Dialog] with a modal transition animation
 */
internal class ModalTransitionDialogHelper {
    var coroutineScope: CoroutineScope? = null
    var onCloseFlow: MutableStateFlow<Boolean>? = null
    fun triggerAnimatedClose() {
        coroutineScope?.launch {
            onCloseFlow?.tryEmit(true)
        }
    }
}

internal const val ANIMATION_TIME = 500L
internal const val DELAY_SHOW_CONTENT = ANIMATION_TIME + 100L

@Composable
internal fun AnimatedModalBottomSheetTransition(
    visible: Boolean,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(Color.White),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var animateContentShowTrigger by remember { mutableStateOf(false) }
    if (visible) {
        LaunchedEffect(key1 = Unit) {
            delay(DELAY_SHOW_CONTENT)
            animateContentShowTrigger = true
        }
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(ANIMATION_TIME.toInt()),
            initialOffsetY = { fullHeight -> fullHeight }
        ),
        exit = slideOutVertically(
            animationSpec = tween(ANIMATION_TIME.toInt()),
            targetOffsetY = { fullHeight -> fullHeight }
        ),
        content = {
            Box(modifier = modifier) {
                if (animateContentShowTrigger)
                    content()
            }

        }
    )
}

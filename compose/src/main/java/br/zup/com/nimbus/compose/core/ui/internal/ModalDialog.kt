package br.zup.com.nimbus.compose.core.ui.internal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

//https://gist.githubusercontent.com/Kaspic/cedb67c68c62526bd14536eae1120f75/raw/9149ab3866ac3fea5121f9d3ac7822e22d9dbae9/ModalTransitionDialog.kt
/**
 * Figured out by trial and error
 */
private const val DIALOG_BUILD_TIME = 300L

/**
 * [Dialog] which uses a modal transition to animate in and out its content.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ModalTransitionDialog(
    onBack: () -> Unit = {},
    onDismissRequest: () -> Unit,
    onLaunch: () -> Unit = {},
    dismissOnBackPress: Boolean = true,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable (ModalTransitionDialogHelper) -> Unit
) {

    val onCloseSharedFlow: MutableSharedFlow<Unit> = remember { MutableSharedFlow() }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val animateContentBackTrigger = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        launch {
            delay(DIALOG_BUILD_TIME)
            animateContentBackTrigger.value = true
        }
        launch {
            onCloseSharedFlow.asSharedFlow().collectLatest { startDismissWithExitAnimation(animateContentBackTrigger, onDismissRequest) }
        }
    }

    Dialog(
        onDismissRequest = { coroutineScope.launch { startDismissWithExitAnimation(animateContentBackTrigger, onDismissRequest) } },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = dismissOnBackPress, dismissOnClickOutside = false)
    ) {
        Box(modifier = modifier) { // Required in order to occupy the whole screen before the animation is triggered
            AnimatedModalBottomSheetTransition(
                onLaunch = onLaunch,
                visible = animateContentBackTrigger.value) {
                content(ModalTransitionDialogHelper(coroutineScope, onCloseSharedFlow))
            }
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
class ModalTransitionDialogHelper(
    private val coroutineScope: CoroutineScope,
    private val onCloseFlow: MutableSharedFlow<Unit>
) {
    fun triggerAnimatedClose() {
        coroutineScope.launch {
            onCloseFlow.emit(Unit)
        }
    }
}

internal const val ANIMATION_TIME = 500L

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun AnimatedModalBottomSheetTransition(
    onLaunch: () -> Unit = {},
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var animateContentShowTrigger by remember { mutableStateOf(false) }
    if(visible) {
        LaunchedEffect(key1 = Unit) {
            delay(ANIMATION_TIME)
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
            if(animateContentShowTrigger)
                content()
        }
    )
}
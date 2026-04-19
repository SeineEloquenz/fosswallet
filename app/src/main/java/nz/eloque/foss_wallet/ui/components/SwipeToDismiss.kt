package nz.eloque.foss_wallet.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun SwipeToDismiss(
    onRightSwipe: () -> Unit,
    onLeftSwipe: () -> Unit,
    modifier: Modifier = Modifier,
    allowRightSwipe: Boolean = true,
    allowLeftSwipe: Boolean = true,
    leftSwipeBackground: @Composable () -> Unit,
    rightSwipeBackground: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val swipeState = rememberSwipeToDismissBoxState()

    val (background, alignment) = when (swipeState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart -> Pair<(@Composable () -> Unit)?, Alignment>(leftSwipeBackground, Alignment.CenterEnd)
        SwipeToDismissBoxValue.StartToEnd -> Pair<(@Composable () -> Unit)?, Alignment>(rightSwipeBackground, Alignment.CenterStart)
        SwipeToDismissBoxValue.Settled -> Pair<(@Composable () -> Unit)?, Alignment>(null, Alignment.CenterEnd)
    }

    SwipeToDismissBox(
        modifier = Modifier.animateContentSize(),
        state = swipeState,
        enableDismissFromStartToEnd = allowRightSwipe,
        enableDismissFromEndToStart = allowLeftSwipe,
        backgroundContent = {
            Box(
                contentAlignment = alignment,
                modifier = Modifier.fillMaxSize()
            ) {
                background?.invoke()
            }
        }
    ) {
        Box(
            modifier = modifier
        ) {
            content()
        }
    }

    when (swipeState.currentValue) {
        SwipeToDismissBoxValue.EndToStart -> {
            LaunchedEffect(swipeState) {
                onLeftSwipe()
                swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
            }
        }

        SwipeToDismissBoxValue.StartToEnd -> {
            LaunchedEffect(swipeState) {
                onRightSwipe()
                swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
            }
        }

        SwipeToDismissBoxValue.Settled -> {
        }
    }
}

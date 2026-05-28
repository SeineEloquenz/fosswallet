package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeToDismiss(
    onRightSwipe: () -> Unit,
    onLeftSwipe: () -> Unit,
    modifier: Modifier = Modifier,
    allowRightSwipe: Boolean = true,
    allowLeftSwipe: Boolean = true,
    leftSwipeBackground: @Composable () -> Unit,
    rightSwipeBackground: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val swipeState = rememberSwipeToDismissBoxState()
    val coroutineScope = rememberCoroutineScope()

    val (background, alignment) =
        when (swipeState.dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> Pair<(@Composable () -> Unit), Alignment>(leftSwipeBackground, Alignment.CenterEnd)
            SwipeToDismissBoxValue.StartToEnd -> Pair<(@Composable () -> Unit), Alignment>(rightSwipeBackground, Alignment.CenterStart)
            SwipeToDismissBoxValue.Settled -> Pair<(@Composable () -> Unit), Alignment>({}, Alignment.CenterEnd)
        }

    SwipeToDismissBox(
        modifier = modifier,
        state = swipeState,
        enableDismissFromStartToEnd = allowRightSwipe,
        enableDismissFromEndToStart = allowLeftSwipe,
        backgroundContent = {
            Box(
                contentAlignment = alignment,
                modifier = Modifier.fillMaxSize().padding(8.dp).alpha(0.25f),
            ) {
                background()
            }
        },
        onDismiss = { dismissDirection ->
            if (dismissDirection == SwipeToDismissBoxValue.StartToEnd) onRightSwipe() else onLeftSwipe()
            coroutineScope.launch { swipeState.reset() }
            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
        },
    ) {
        content()
    }
}

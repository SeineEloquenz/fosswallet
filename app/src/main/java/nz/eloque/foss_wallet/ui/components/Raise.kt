package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun Raise(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            decorFitsSystemWindows = true,
        ),
    ) {
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(1f)
        Box(modifier
        ) {
            content()
        }
    }
}
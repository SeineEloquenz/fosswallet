package nz.eloque.foss_wallet.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun AllowOnLockscreen(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(Unit) {
        activity.setShowWhenLocked(true)

        onDispose {
            activity.setShowWhenLocked(false)
        }
    }

    content()
}
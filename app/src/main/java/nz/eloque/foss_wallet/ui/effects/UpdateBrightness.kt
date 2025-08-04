package nz.eloque.foss_wallet.ui.effects

import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun UpdateBrightness() {
    val activity = LocalContext.current.getActivity()
    DisposableEffect(Unit) {
        setBrightness(activity, true)
        onDispose {
            setBrightness(activity, false)
        }
    }
}

fun setBrightness(activity: ComponentActivity?, isFull: Boolean) {
    if (activity == null) return
    val layoutParams: WindowManager.LayoutParams = activity.window.attributes
    layoutParams.screenBrightness = if (isFull) 1.0f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    activity.window.attributes = layoutParams
}


fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
package nz.eloque.foss_wallet.ui.components

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsRequired(
    multiplePermissionsState: MultiplePermissionsState,
    permissionsNotGrantedContent: @Composable (() -> Unit),
    permissionsNotAvailableContent: @Composable (() -> Unit),
    content: @Composable (() -> Unit),
) {
    when {
        multiplePermissionsState.allPermissionsGranted -> {
            content()
        }
        multiplePermissionsState.shouldShowRationale ||
                !multiplePermissionsState.allPermissionsGranted ->
        {
            permissionsNotGrantedContent()
        }
        else -> {
            permissionsNotAvailableContent()
        }
    }
}

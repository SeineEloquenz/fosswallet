package nz.eloque.foss_wallet.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import nz.eloque.foss_wallet.R

/** Warns that [formats] cannot be rendered and will be replaced with QR codes, until the user acknowledges it. */
@Composable
fun BarcodeFallbackDialog(
    formats: Set<String>,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(R.string.barcode_format_fallback_title)) },
        text = { Text(stringResource(R.string.barcode_format_fallback_warning, formats.joinToString("\n"))) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(android.R.string.ok))
            }
        },
    )
}

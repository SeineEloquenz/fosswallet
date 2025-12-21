package nz.eloque.foss_wallet.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.effects.UpdateBrightness
import nz.eloque.foss_wallet.ui.screens.pass.PassImage

@Composable
fun FullscreenBarcode(
    image: Bitmap,
    barcodePosition: BarcodePosition,
    increaseBrightness: Boolean,
    isFullscreen: Boolean,
    onDismiss: () -> Unit,
) {


    if (isFullscreen) {
        Raise(onDismiss = onDismiss) {
            if (increaseBrightness) {
                UpdateBrightness()
            }
            PassImage(
                bitmap = image,
                barcodePosition = barcodePosition,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
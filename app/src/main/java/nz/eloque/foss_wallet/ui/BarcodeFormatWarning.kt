package nz.eloque.foss_wallet.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import kotlin.coroutines.resume

internal suspend fun showBarcodeFormatWarning(
    context: Context,
    formats: Set<String>,
) = withContext(Dispatchers.Main) {
    suspendCancellableCoroutine<Unit> { continuation ->
        val dialog =
            AlertDialog
                .Builder(context)
                .setTitle(R.string.barcode_format_fallback_title)
                .setMessage(context.getString(R.string.barcode_format_fallback_warning, formats.joinToString("\n")))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ -> continuation.resume(Unit) }
                .create()

        continuation.invokeOnCancellation {
            Handler(Looper.getMainLooper()).post { dialog.dismiss() }
        }
        dialog.show()
    }
}

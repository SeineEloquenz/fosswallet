package nz.eloque.foss_wallet.shortcut

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass

private const val HOST = "pass"
const val SCHEME = "fosswallet"
const val SHORTCUT_BASE_URI = "$SCHEME://$HOST"

/**
 * Pins a home-screen shortcut for [pass] that deep-links via [SHORTCUT_BASE_URI]/{passId}.
 *
 * Re-requesting the same pass is safe, a failed attempt can always be retried.
 */
fun Context.createShortcut(pass: Pass) {
    if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
        toast(R.string.shortcut_unsupported)
        return
    }

    val icon = pass.adaptiveIcon(this)
    if (icon == null) {
        toast(R.string.shortcut_failed)
        return
    }

    val shortcutName = pass.description
    val shortcut =
        ShortcutInfoCompat
            .Builder(this, pass.shortcutId())
            .setShortLabel(shortcutName)
            .setLongLabel(shortcutName)
            .setIcon(icon)
            .setIntent(pass.viewIntent(this))
            .build()

    ShortcutManagerCompat.requestPinShortcut(this, shortcut, null)
}

/** A pinned shortcut cannot be removed from the home screen programmatically. */
fun Context.disableShortcut(pass: Pass) {
    ShortcutManagerCompat.disableShortcuts(this, listOf(pass.shortcutId()), "")
}

private fun Pass.shortcutId(): String = "pass_$id"

private fun Pass.viewIntent(context: Context): Intent =
    Intent(
        Intent.ACTION_VIEW,
        "$SHORTCUT_BASE_URI/$id".toUri(),
        context,
        MainActivity::class.java,
    )

private fun Pass.adaptiveIcon(context: Context): IconCompat? {
    val bitmap = BitmapFactory.decodeFile(iconFile(context).path) ?: return null
    return IconCompat.createWithAdaptiveBitmap(wrapInAdaptiveBounds(bitmap))
}

private fun wrapInAdaptiveBounds(source: Bitmap): Bitmap {
    // Adaptive icons require the main content to be centered (roughly 60-70% of the total size)
    val size = source.width.coerceAtLeast(source.height)
    val newSize = (size * 1.5).toInt() // Increase canvas size to create margins

    val output = createBitmap(newSize, newSize)
    val canvas = Canvas(output)
    canvas.drawColor(Color.WHITE)

    val left = (newSize - source.width) / 2f
    val top = (newSize - source.height) / 2f
    canvas.drawBitmap(source, left, top, null)
    return output
}

internal fun Context.toast(
    @StringRes message: Int,
) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

package nz.eloque.foss_wallet.shortcut

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass

object Shortcut {

    const val SCHEME = "fosswallet"
    const val HOST = "pass"
    const val BASE_URI = "$SCHEME://$HOST"

    fun create(context: Context, pass: Pass, shortcutName: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "${BASE_URI}/${pass.id}".toUri(),
            context,
            MainActivity::class.java
        )

        val originalBitmap = BitmapFactory.decodeFile(pass.iconFile(context).toPath().toString())
        val adaptiveBitmap = wrapInAdaptiveBounds(originalBitmap)

        val shortcutIcon = IconCompat.createWithAdaptiveBitmap(adaptiveBitmap)
        val shortcut = ShortcutInfoCompat.Builder(context, pass.shortcutId())
            .setShortLabel(shortcutName)
            .setLongLabel(shortcutName)
            .setIcon(shortcutIcon)
            .setIntent(intent)
            .build()

        val existingShortcuts = ShortcutManagerCompat.getShortcuts(
            context,
            ShortcutManagerCompat.FLAG_MATCH_DYNAMIC or ShortcutManagerCompat.FLAG_MATCH_PINNED
        )
        if (existingShortcuts.any { it.id == pass.shortcutId() }) {
            Toast.makeText(context, R.string.shortcut_exists, Toast.LENGTH_SHORT).show()
            return
        }

        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun exists(context: Context, pass: Pass): Boolean {
        val shortcuts = ShortcutManagerCompat.getShortcuts(
            context,
            ShortcutManagerCompat.FLAG_MATCH_DYNAMIC or ShortcutManagerCompat.FLAG_MATCH_PINNED
        )
        return shortcuts.any { it.id == pass.shortcutId() }
    }

    fun remove(context: Context, pass: Pass) {
        ShortcutManagerCompat.disableShortcuts(context, listOf(pass.shortcutId()), "")
    }

    private fun Pass.shortcutId(): String {
        return "pass_${this.id}"
    }

    private fun wrapInAdaptiveBounds(source: Bitmap): Bitmap {
        // Adaptive icons require the main content to be centered (roughly 60-70% of the total size)
        val size = source.width.coerceAtLeast(source.height)
        val newSize = (size * 1.5).toInt() // Increase canvas size to create margins

        val output = Bitmap.createBitmap(newSize, newSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        canvas.drawColor(Color.WHITE)

        val left = (newSize - source.width) / 2f
        val top = (newSize - source.height) / 2f

        canvas.drawBitmap(source, left, top, null)
        return output
    }
}
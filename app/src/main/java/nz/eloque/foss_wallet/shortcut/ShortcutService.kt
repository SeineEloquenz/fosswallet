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
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.shortcut.ShortcutService.Companion.BASE_URI

/**
 * Home-screen shortcuts that deep-link into a single pass via [BASE_URI]/{passId}.
 */
class ShortcutService
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        /**
         * Asks the launcher to pin a shortcut for [pass] to the home screen.
         *
         * Re-requesting the same pass is safe, a failed attempt can always be retried.
         */
        fun create(
            pass: Pass,
            shortcutName: String,
        ) {
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                toast(R.string.shortcut_unsupported)
                return
            }

            val icon = pass.adaptiveIcon()
            if (icon == null) {
                toast(R.string.shortcut_failed)
                return
            }

            val shortcut =
                ShortcutInfoCompat
                    .Builder(context, pass.shortcutId())
                    .setShortLabel(shortcutName)
                    .setLongLabel(shortcutName)
                    .setIcon(icon)
                    .setIntent(pass.viewIntent())
                    .build()

            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        }

        /**
         * Disables the shortcut for [pass].
         *
         * A pinned shortcut cannot be removed from the home screen programmatically
         */
        fun disable(pass: Pass) {
            ShortcutManagerCompat.disableShortcuts(context, listOf(pass.shortcutId()), "")
        }

        private fun Pass.shortcutId(): String = "pass_$id"

        private fun Pass.viewIntent(): Intent =
            Intent(
                Intent.ACTION_VIEW,
                "$BASE_URI/$id".toUri(),
                context,
                MainActivity::class.java,
            )

        private fun Pass.adaptiveIcon(): IconCompat? {
            val bitmap = BitmapFactory.decodeFile(iconFile(context).path) ?: return null
            return IconCompat.createWithAdaptiveBitmap(wrapInAdaptiveBounds(bitmap))
        }

        private fun toast(
            @StringRes message: Int,
        ) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

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

        companion object {
            const val SCHEME = "fosswallet"
            const val HOST = "pass"
            const val BASE_URI = "$SCHEME://$HOST"
        }
    }

package nz.eloque.foss_wallet.launcher

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.launcher.PassCardWidgetPrefs
import nz.eloque.foss_wallet.launcher.Widget
import nz.eloque.foss_wallet.launcher.WidgetReceiver
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.shortcut.LauncherService.Companion.BASE_URI

/**
 * Home-screen entry points for a single pass: pinned shortcuts via [BASE_URI]/{passId}
 * and pinned pass-card widgets.
 */
class LauncherService
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        /**
         * Asks the launcher to pin a shortcut for [pass] to the home screen.
         *
         * Re-requesting the same pass is safe, a failed attempt can always be retried.
         */
        fun createShortcut(pass: Pass) {
            if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                toast(R.string.shortcut_unsupported)
                return
            }

            val icon = pass.adaptiveIcon()
            if (icon == null) {
                toast(R.string.shortcut_failed)
                return
            }

            val shortcutName = pass.description
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
         * A pinned shortcut cannot be removed from the home screen programmatically.
         */
        fun disableShortcut(pass: Pass) {
            ShortcutManagerCompat.disableShortcuts(context, listOf(pass.shortcutId()), "")
        }

        /**
         * Asks the launcher to pin the pass-card widget, pre-bound to [pass].
         *
         * The launcher shows its standard widget preview. Once the user drops it,
         * [WidgetPinReceiver] fires and writes [pass]'s id into that widget instance's
         * Glance state, skipping the manual widget-configuration picker.
         */
        fun createWidget(pass: Pass) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (!appWidgetManager.isRequestPinAppWidgetSupported) {
                toast(R.string.widget_unsupported)
                return
            }

            val provider = ComponentName(context, WidgetReceiver::class.java)

            val callbackIntent =
                Intent(context, WidgetPinReceiver::class.java).apply {
                    putExtra(MainActivity.EXTRA_PASS_ID, pass.id)
                    // AppWidgetManager.EXTRA_APPWIDGET_ID is appended to this intent by the system.
                }
            val successCallback =
                PendingIntent.getBroadcast(
                    context,
                    pass.id.hashCode(), // unique per pass, otherwise PendingIntents overwrite each other
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            appWidgetManager.requestPinAppWidget(provider, null, successCallback)
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

/**
 * Fires after the launcher successfully pins a widget requested via
 * [LauncherService.createWidget]. Writes the pre-selected pass into that widget
 * instance's Glance state so the user never sees the manual widget-configuration picker.
 */
@AndroidEntryPoint
class WidgetPinReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val appWidgetId =
            intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val passId = intent.getStringExtra(MainActivity.EXTRA_PASS_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || passId == null) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[PassCardWidgetPrefs.PASS_ID_KEY] = passId
                    prefs[PassCardWidgetPrefs.SHOWING_BACK_KEY] = false
                }
                Widget().update(context, glanceId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

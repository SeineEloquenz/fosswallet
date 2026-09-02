package nz.eloque.foss_wallet.launcher

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
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
import nz.eloque.foss_wallet.model.Pass

private const val EXTRA_SHOW_BARCODE = "nz.eloque.foss_wallet.EXTRA_SHOW_BARCODE"
private const val TAG = "LauncherService"

/**
 * Home-screen entry points for a single pass: pinned shortcuts (see Shortcut.kt)
 * and pinned pass-card widgets.
 */
class LauncherService
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        fun createShortcut(pass: Pass) = context.createShortcut(pass)

        fun disableShortcut(pass: Pass) = context.disableShortcut(pass)

        /**
         * Asks the launcher to pin the pass-card widget, pre-bound to [pass].
         * [showBarcode] selects the barcode-side widget instead of the default front widget.
         *
         * Two mechanisms try to apply [pass] to the freshly pinned widget without the user
         * seeing the manual picker:
         * 1. [WidgetPinReceiver] — fires if the launcher invokes requestPinAppWidget's
         *    successCallback. Not every launcher does this reliably.
         * 2. [PendingWidgetConfig] — a short-lived fallback consulted directly in
         *    PassCardWidget/BarcodeWidget.provideGlance() (Widget.kt) on the widget's first
         *    render, and again in WidgetConfigActivity if the user taps an unconfigured
         *    widget before that first render lands.
         */
        fun createWidget(
            pass: Pass,
            showBarcode: Boolean = false,
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (!appWidgetManager.isRequestPinAppWidgetSupported) {
                context.toast(R.string.widget_unsupported)
                return
            }
            val receiverClass =
                if (showBarcode) BarcodeWidgetReceiver::class.java else PassCardWidgetReceiver::class.java
            val provider = ComponentName(context, receiverClass)
            val callbackIntent =
                Intent(context, WidgetPinReceiver::class.java).apply {
                    putExtra(MainActivity.EXTRA_PASS_ID, pass.id)
                    putExtra(EXTRA_SHOW_BARCODE, showBarcode)
                    // AppWidgetManager.EXTRA_APPWIDGET_ID is appended to this intent by the system.
                }
            val successCallback =
                PendingIntent.getBroadcast(
                    context,
                    // unique per pass+type, otherwise PendingIntents for the front and
                    // barcode widget of the same pass would overwrite each other
                    31 * pass.id.hashCode() + showBarcode.hashCode(),
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            PendingWidgetConfig.set(context, pass.id)
            Log.d(TAG, "requestPinAppWidget: provider=$provider passId=${pass.id} showBarcode=$showBarcode")
            val requested = appWidgetManager.requestPinAppWidget(provider, null, successCallback)
            Log.d(TAG, "requestPinAppWidget returned $requested")
        }
    }

/**
 * Fires after the launcher successfully pins a widget requested via
 * [LauncherService.createWidget]. Writes the pre-selected pass into that widget
 * instance's Glance state so the user never sees the manual widget-configuration picker.
 *
 * Not invoked by every launcher — see [PendingWidgetConfig] for the fallback path.
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
        val showBarcode = intent.getBooleanExtra(EXTRA_SHOW_BARCODE, false)
        Log.d("WidgetPinReceiver", "onReceive appWidgetId=$appWidgetId passId=$passId showBarcode=$showBarcode")
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || passId == null) {
            Log.w("WidgetPinReceiver", "Aborting: invalid appWidgetId or missing passId")
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[PassCardWidgetPrefs.PASS_ID_KEY] = passId
                }
                Log.d("WidgetPinReceiver", "Wrote passId=$passId for glanceId=$glanceId")
                if (showBarcode) {
                    BarcodeWidget().update(context, glanceId)
                } else {
                    PassCardWidget().update(context, glanceId)
                }
                Log.d("WidgetPinReceiver", "Widget updated")
                PendingWidgetConfig.clearIfMatches(context, passId)
            } catch (e: Exception) {
                Log.e("WidgetPinReceiver", "Failed to configure widget", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * Short-lived, single-slot record of "the pass the user most recently asked to pin
 * a widget for". Fallback for launchers that don't invoke requestPinAppWidget's
 * successCallback ([WidgetPinReceiver]).
 *
 * Consulted from two places:
 * - PassCardWidget/BarcodeWidget.provideGlance() (Widget.kt), on the widget's first
 *   render right after pinning — the common case, since the launcher does call
 *   APPWIDGET_UPDATE even when it skips requestPinAppWidget's successCallback.
 * - WidgetConfigActivity, if the user taps the "unconfigured" placeholder before
 *   that first render has applied it (e.g. a very slow provider process).
 *
 * Single-slot by design — if the user pins two widgets in quick succession before
 * either one's first render lands, only the most recent pending pass is recoverable
 * this way. Rare in practice and not worse than the previous behavior.
 */
internal object PendingWidgetConfig {
    private const val PREFS_NAME = "widget_pending_config"
    private const val KEY_PASS_ID = "pass_id"
    private const val KEY_TIMESTAMP = "timestamp"
    private const val MAX_AGE_MILLIS = 5 * 60 * 1000L // 5 minutes

    fun set(
        context: Context,
        passId: String,
    ) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_PASS_ID, passId)
                    .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            }
    }

    /**
     * Returns the pending pass id if one exists and is recent, without clearing it —
     * callers only clear it once the widget is actually configured (see [clearIfMatches]),
     * so a retry after a transient failure can still pick it up.
     */
    fun peek(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val passId = prefs.getString(KEY_PASS_ID, null) ?: return null
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0)
        if (System.currentTimeMillis() - timestamp > MAX_AGE_MILLIS) return null
        return passId
    }

    fun clearIfMatches(
        context: Context,
        passId: String,
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_PASS_ID, null) == passId) {
            prefs.edit { clear() }
        }
    }
}

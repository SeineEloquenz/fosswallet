package nz.eloque.foss_wallet.launcher

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import nz.eloque.foss_wallet.shortcut.createShortcut
import nz.eloque.foss_wallet.shortcut.disableShortcut
import nz.eloque.foss_wallet.shortcut.toast

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
         *
         * The launcher shows its standard widget preview. Once the user drops it,
         * [WidgetPinReceiver] fires and writes [pass]'s id into that widget instance's
         * Glance state, skipping the manual widget-configuration picker.
         */
        fun createWidget(pass: Pass) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (!appWidgetManager.isRequestPinAppWidgetSupported) {
                context.toast(R.string.widget_unsupported)
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

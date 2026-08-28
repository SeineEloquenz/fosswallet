package nz.eloque.foss_wallet.launcher

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.contentprovider.ProviderEntrypoint
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.ui.glance.Barcode
import nz.eloque.foss_wallet.ui.glance.PassCard
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import androidx.glance.text.Text as GlanceText

object PassCardWidgetPrefs {
    val PASS_ID_KEY = stringPreferencesKey("pass_id")
}

// Glance writes ActionParameters as Intent extras under the same name.
val PASS_ID_PARAM = ActionParameters.Key<String>(MainActivity.EXTRA_PASS_ID)
val SHOW_BARCODE_PARAM = ActionParameters.Key<Boolean>(MainActivity.EXTRA_SHOW_BARCODE)

// Shared 2:1 aspect-ratio resizing steps, kept in sync with minWidth/minHeight and
// maxResizeWidth/maxResizeHeight in the widgets' info.xml files.
private val WIDGET_SIZE_MODE =
    SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 90.dp),
            DpSize(270.dp, 135.dp),
            DpSize(360.dp, 180.dp),
        ),
    )

// Square resizing steps for the Barcode widget.
private val BARCODE_SIZE_MODE =
    SizeMode.Responsive(
        setOf(
            DpSize(90.dp, 90.dp),
            DpSize(135.dp, 135.dp),
            DpSize(180.dp, 180.dp),
        ),
    )

private fun Context.passRepository(): PassRepository =
    EntryPointAccessors
        .fromApplication(applicationContext, ProviderEntrypoint::class.java)
        .passRepository()

/**
 * If this widget instance has no pass configured yet, checks [PendingWidgetConfig] for
 * a recent match from LauncherService.createWidget and writes it into the widget's own
 * state — this is what lets a freshly pinned widget show the right pass on its very
 * first render, without the user needing to tap the "unconfigured" placeholder first.
 * No-op if the widget is already configured or there's no recent pending match.
 */
private suspend fun GlanceAppWidget.applyPendingConfigIfNeeded(
    context: Context,
    id: GlanceId,
) {
    val existingPassId =
        runCatching {
            getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[PassCardWidgetPrefs.PASS_ID_KEY]
        }.getOrNull()
    if (existingPassId != null) return

    val pendingPassId = PendingWidgetConfig.peek(context) ?: return
    updateAppWidgetState(context, id) { prefs ->
        prefs[PassCardWidgetPrefs.PASS_ID_KEY] = pendingPassId
    }
    PendingWidgetConfig.clearIfMatches(context, pendingPassId)
}

// No constructor parameter for PassRepository: GlanceAppWidgetReceiver.glanceAppWidget is a
// getter without a Context, so it's resolved here in provideGlance()/providePreview() via
// ProviderEntrypoint instead (same pattern as CatimaContentProvider.kt).
class PassCardWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = WIDGET_SIZE_MODE

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        applyPendingConfigIfNeeded(context, id)

        val passRepository = context.passRepository()

        provideContent {
            val prefs = currentState<Preferences>()
            val passId = prefs[PassCardWidgetPrefs.PASS_ID_KEY]

            // provideContent's lambda is @Composable, not suspend — the async
            // repository lookup has to happen inside produceState's coroutine.
            val localizedPassState by produceState<LocalizedPassWithTags?>(initialValue = null, key1 = passId) {
                value = passId?.let { withContext(Dispatchers.IO) { passRepository.findById(it) } }
            }
            val localizedPass = localizedPassState

            if (localizedPass == null) {
                UnconfiguredWidgetContent(id)
            } else {
                PassCard(
                    localizedPass = localizedPass,
                    context = context,
                    onClick =
                        actionStartActivity<MainActivity>(
                            parameters = actionParametersOf(PASS_ID_PARAM to localizedPass.pass.id),
                        ),
                )
            }
        }
    }

    // Single composition, no recomposition or effects — used only to render the
    // widget-picker preview on Android 15+ (see GlanceAppWidgetManager.setWidgetPreviews).
    // Falls back to previewImage in the widget's info.xml on older versions and
    // whenever the user has no passes yet.
    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val passRepository = context.passRepository()
        val samplePass =
            withContext(Dispatchers.IO) {
                passRepository.all().first().firstOrNull()
            }?.applyLocalization(Locale.getDefault().language)

        provideContent {
            if (samplePass == null) {
                UnconfiguredWidgetContent(id = null)
            } else {
                PassCard(
                    localizedPass = samplePass,
                    context = context,
                    onClick = actionStartActivity<MainActivity>(),
                )
            }
        }
    }
}

class BarcodeWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = BARCODE_SIZE_MODE

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        applyPendingConfigIfNeeded(context, id)

        val passRepository = context.passRepository()

        provideContent {
            val prefs = currentState<Preferences>()
            val passId = prefs[PassCardWidgetPrefs.PASS_ID_KEY]

            val localizedPassState by produceState<LocalizedPassWithTags?>(initialValue = null, key1 = passId) {
                value = passId?.let { withContext(Dispatchers.IO) { passRepository.findById(it) } }
            }
            val localizedPass = localizedPassState

            if (localizedPass == null) {
                UnconfiguredWidgetContent(id)
            } else {
                val barcodeBitmap =
                    remember(localizedPass.pass.barCodes) {
                        localizedPass.pass.barCodes
                            .firstOrNull()
                            ?.toBitmap(width = 256, height = 256)
                    }
                Barcode(
                    localizedPass = localizedPass,
                    barcodeBitmap = barcodeBitmap,
                )
            }
        }
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val passRepository = context.passRepository()
        val samplePass =
            withContext(Dispatchers.IO) {
                passRepository.all().first().firstOrNull()
            }?.applyLocalization(Locale.getDefault().language)

        provideContent {
            if (samplePass == null) {
                UnconfiguredWidgetContent(id = null)
            } else {
                val barcodeBitmap =
                    remember(samplePass.pass.barCodes) {
                        samplePass.pass.barCodes
                            .firstOrNull()
                            ?.toBitmap(width = 256, height = 256)
                    }
                Barcode(
                    localizedPass = samplePass,
                    barcodeBitmap = barcodeBitmap,
                )
            }
        }
    }
}

/**
 * Publishes updated widget-picker previews from a real pass, if one exists, for both
 * widgets.
 *
 * Call this after data that would change the preview changes (e.g. import, delete)
 * or on app start. No-op below Android 15. The API is rate-limited to roughly two
 * calls per hour per receiver; a [GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED]
 * result just means the previously published preview stays visible.
 */
suspend fun refreshWidgetPreview(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        return
    }
    val manager = GlanceAppWidgetManager(context)
    manager.setWidgetPreviews(PassCardWidgetReceiver::class)
    manager.setWidgetPreviews(BarcodeWidgetReceiver::class)
}

// Tappable "unconfigured" placeholder — the remaining fallback for the rare case where
// applyPendingConfigIfNeeded() hasn't run yet (e.g. a very slow provider process) or
// there's no pending config at all (widget added via the plain home-screen picker).
// Tapping it opens WidgetConfigActivity directly.
// [id] is null in providePreview(), where there's no real appWidgetId to configure yet.
@Composable
private fun UnconfiguredWidgetContent(id: GlanceId?) {
    val context = LocalContext.current
    val modifier =
        if (id != null) {
            val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
            val configureIntent =
                Intent(context, WidgetConfigActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
            GlanceModifier.fillMaxSize().padding(8.dp).clickable(actionStartActivity(configureIntent))
        } else {
            GlanceModifier.fillMaxSize().padding(8.dp)
        }
    Box(modifier = modifier) {
        GlanceText(text = context.getString(R.string.widget_unconfigured))
    }
}

class PassCardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PassCardWidget()
}

class BarcodeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BarcodeWidget()
}

@HiltViewModel
class WidgetConfigViewModel
    @Inject
    constructor(
        passRepository: PassRepository,
    ) : ViewModel() {
        val passes: StateFlow<List<LocalizedPassWithTags>> =
            passRepository
                .all()
                .map { list -> list.map { it.applyLocalization(Locale.getDefault().language) } }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

// Started via android:configure in each widget's info.xml, and also reachable by
// tapping an unconfigured widget directly (see UnconfiguredWidgetContent) — needed
// for the rare case where the widget's own provideGlance() (see
// applyPendingConfigIfNeeded in this file) hasn't applied a pending config yet.
//
// Resolution order on open:
// 1. Already configured? — finish immediately.
// 2. A recent PendingWidgetConfig match? — apply it and finish, without the user
//    ever seeing the picker.
// 3. Otherwise, show the manual picker.
@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {
    private val viewModel: WidgetConfigViewModel by viewModels()

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        appWidgetId =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        lifecycleScope.launch {
            val alreadyConfigured =
                withContext(Dispatchers.IO) {
                    var configured = false
                    repeat(5) { attempt ->
                        configured =
                            runCatching {
                                val manager = GlanceAppWidgetManager(this@WidgetConfigActivity)
                                val glanceId = manager.getGlanceIdBy(appWidgetId)
                                val prefs =
                                    getAppWidgetState(
                                        this@WidgetConfigActivity,
                                        PreferencesGlanceStateDefinition,
                                        glanceId,
                                    )
                                prefs[PassCardWidgetPrefs.PASS_ID_KEY] != null
                            }.getOrDefault(false)
                        if (configured || attempt == 4) return@repeat
                        delay(99.milliseconds)
                    }
                    configured
                }

            if (alreadyConfigured) {
                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(RESULT_OK, resultValue)
                finish()
                return@launch
            }

            val pendingPassId = withContext(Dispatchers.IO) { PendingWidgetConfig.peek(this@WidgetConfigActivity) }
            if (pendingPassId != null) {
                applyPass(pendingPassId)
                return@launch
            }

            setContent {
                WalletTheme {
                    Surface {
                        val passes by viewModel.passes.collectAsState()
                        PassPickerScreen(passes = passes, onPassSelected = { applyPass(it.pass.id) })
                    }
                }
            }
        }
    }

    private fun applyPass(passId: String) {
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@WidgetConfigActivity)
            val glanceId = manager.getGlanceIdBy(appWidgetId)

            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { prefs ->
                prefs[PassCardWidgetPrefs.PASS_ID_KEY] = passId
            }

            // Figure out which of the two widgets this id belongs to and update only that one.
            val providerInfo = AppWidgetManager.getInstance(this@WidgetConfigActivity).getAppWidgetInfo(appWidgetId)
            when (providerInfo?.provider?.className) {
                PassCardWidgetReceiver::class.java.name -> PassCardWidget().update(this@WidgetConfigActivity, glanceId)
                BarcodeWidgetReceiver::class.java.name -> BarcodeWidget().update(this@WidgetConfigActivity, glanceId)
            }

            withContext(Dispatchers.IO) {
                PendingWidgetConfig.clearIfMatches(this@WidgetConfigActivity, passId)
            }

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}

@Composable
private fun PassPickerScreen(
    passes: List<LocalizedPassWithTags>,
    onPassSelected: (LocalizedPassWithTags) -> Unit,
) {
    LazyColumn {
        items(passes, key = { it.pass.id }) { pass ->
            ListItem(
                modifier = Modifier.clickable { onPassSelected(pass) },
            ) {
                Text(pass.pass.description)
            }
        }
    }
}

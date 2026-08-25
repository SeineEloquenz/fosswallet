package nz.eloque.foss_wallet.launcher

import android.appwidget.AppWidgetManager
import android.content.Context
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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
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
import nz.eloque.foss_wallet.ui.glance.PassCardBack
import nz.eloque.foss_wallet.ui.glance.PassCardFront
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import java.util.Locale
import androidx.glance.text.Text as GlanceText

object PassCardWidgetPrefs {
    val PASS_ID_KEY = stringPreferencesKey("pass_id")
    val SHOWING_BACK_KEY = booleanPreferencesKey("showing_back")
}

// Glance writes ActionParameters as Intent extras under the same name.
private val PASS_ID_PARAM = ActionParameters.Key<String>(MainActivity.EXTRA_PASS_ID)
private val SHOW_BARCODE_PARAM = ActionParameters.Key<Boolean>(MainActivity.EXTRA_SHOW_BARCODE)

// Flips the widget between front and back for whichever glanceId triggered it.
class ToggleCardSideAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentlyShowingBack = prefs[PassCardWidgetPrefs.SHOWING_BACK_KEY] ?: false
            prefs[PassCardWidgetPrefs.SHOWING_BACK_KEY] = !currentlyShowingBack
        }
        Widget().update(context, glanceId)
    }
}

// No constructor parameter for PassRepository: GlanceAppWidgetReceiver.glanceAppWidget is a
// getter without a Context, so it's resolved here in provideGlance()/providePreview() via
// ProviderEntrypoint instead (same pattern as CatimaContentProvider.kt).
class Widget : GlanceAppWidget() {
    // Stepped resizing while keeping a fixed 2:1 aspect ratio at every stage.
    // Keep these DpSize buckets in sync with minWidth/minHeight and
    // maxResizeWidth/maxResizeHeight in passcard_widget_info.xml.
    override val sizeMode: SizeMode =
        SizeMode.Responsive(
            setOf(
                DpSize(180.dp, 90.dp),
                DpSize(270.dp, 135.dp),
                DpSize(360.dp, 180.dp),
            ),
        )

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val passRepository = context.passRepository()

        provideContent {
            val prefs = currentState<Preferences>()
            val passId = prefs[PassCardWidgetPrefs.PASS_ID_KEY]
            val showingBack = prefs[PassCardWidgetPrefs.SHOWING_BACK_KEY] ?: false

            // provideContent's lambda is @Composable, not suspend — the async
            // repository lookup has to happen inside produceState's coroutine.
            val localizedPassState by produceState<LocalizedPassWithTags?>(initialValue = null, key1 = passId) {
                value = passId?.let { withContext(Dispatchers.IO) { passRepository.findById(it) } }
            }
            val localizedPass = localizedPassState

            if (localizedPass == null) {
                UnconfiguredWidgetContent()
            } else if (showingBack) {
                val barcodeBitmap =
                    remember(localizedPass.pass.barCodes) {
                        localizedPass.pass.barCodes
                            .firstOrNull()
                            ?.toBitmap(width = 256, height = 256)
                    }
                PassCardBack(
                    localizedPass = localizedPass,
                    context = context,
                    barcodeBitmap = barcodeBitmap,
                    onClick =
                        actionStartActivity<MainActivity>(
                            parameters =
                                actionParametersOf(
                                    PASS_ID_PARAM to localizedPass.pass.id,
                                    SHOW_BARCODE_PARAM to true,
                                ),
                        ),
                    onFlipToFront = actionRunCallback<ToggleCardSideAction>(),
                )
            } else {
                PassCardFront(
                    localizedPass = localizedPass,
                    context = context,
                    onClick =
                        actionStartActivity<MainActivity>(
                            parameters = actionParametersOf(PASS_ID_PARAM to localizedPass.pass.id),
                        ),
                    onFlipToBack = actionRunCallback<ToggleCardSideAction>(),
                )
            }
        }
    }

    // Single composition, no recomposition or effects — used only to render the
    // widget-picker preview on Android 15+ (see GlanceAppWidgetManager.setWidgetPreviews).
    // Falls back to previewImage in passcard_widget_info.xml on older versions and
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
                UnconfiguredWidgetContent()
            } else {
                PassCardFront(
                    localizedPass = samplePass,
                    context = context,
                    onClick = actionStartActivity<MainActivity>(),
                    onFlipToBack = actionStartActivity<MainActivity>(),
                )
            }
        }
    }

    private fun Context.passRepository(): PassRepository =
        EntryPointAccessors
            .fromApplication(applicationContext, ProviderEntrypoint::class.java)
            .passRepository()
}

/**
 * Publishes an updated widget-picker preview from a real pass, if one exists.
 *
 * Call this after data that would change the preview changes (e.g. import, delete)
 * or on app start. No-op below Android 15. The API is rate-limited to roughly two
 * calls per hour; a [GlanceAppWidgetManager.SET_WIDGET_PREVIEWS_RESULT_RATE_LIMITED]
 * result just means the previously published preview stays visible.
 */
suspend fun refreshWidgetPreview(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        return
    }
    GlanceAppWidgetManager(context).setWidgetPreviews(WidgetReceiver::class)
}

@Composable
private fun UnconfiguredWidgetContent() {
    val context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
        GlanceText(text = context.getString(R.string.widget_unconfigured))
    }
}

class WidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget()
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

// Started via android:configure in passcard_widget_info.xml.
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

        setContent {
            WalletTheme {
                Surface {
                    val passes by viewModel.passes.collectAsState()
                    PassPickerScreen(passes = passes, onPassSelected = ::onPassSelected)
                }
            }
        }
    }

    private fun onPassSelected(pass: LocalizedPassWithTags) {
        lifecycleScope.launch {
            val glanceId =
                GlanceAppWidgetManager(this@WidgetConfigActivity)
                    .getGlanceIdBy(appWidgetId)

            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { prefs ->
                prefs[PassCardWidgetPrefs.PASS_ID_KEY] = pass.pass.id
                prefs[PassCardWidgetPrefs.SHOWING_BACK_KEY] = false
            }
            Widget().update(this@WidgetConfigActivity, glanceId)

            val resultValue = android.content.Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
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

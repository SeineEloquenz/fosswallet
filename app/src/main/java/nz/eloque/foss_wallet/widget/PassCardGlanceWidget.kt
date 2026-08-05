package nz.eloque.foss_wallet.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text as GlanceText
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.contentprovider.ProviderEntrypoint
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.ui.card.glance.PassCardGlance
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import java.util.Locale

/**
 * All building blocks of the PassCard app widget, bundled in one file (same pattern as
 * contentprovider/CatimaContentProvider.kt). androidx.compose.material3.Text and
 * androidx.glance.text.Text share a simple name, so the latter is imported as GlanceText.
 */

object PassCardWidgetPrefs {
    val PASS_ID_KEY = stringPreferencesKey("pass_id")
}

/**
 * Shows one, per-widget-instance configurable pass (2:1, showEntirePass=false). Size comes from
 * passcard_widget_info.xml (resizeMode="none"). Which pass to show is stored per GlanceId in the
 * preferences DataStore under PassCardWidgetPrefs.PASS_ID_KEY (set in
 * PassCardWidgetConfigActivity).
 *
 * No constructor parameter for PassRepository: GlanceAppWidgetReceiver.glanceAppWidget is a
 * getter without a Context (BroadcastReceiver holds no Context as a property), so the
 * repository can't be injected there. It's resolved here instead, in provideGlance(), where
 * `context` is available - via the same ProviderEntrypoint Hilt interface CatimaContentProvider.kt
 * already uses for the same purpose.
 */
class PassCardGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val passRepository =
            EntryPointAccessors
                .fromApplication(context.applicationContext, ProviderEntrypoint::class.java)
                .passRepository()

        provideContent {
            val prefs = currentState<Preferences>()
            val passId = prefs[PassCardWidgetPrefs.PASS_ID_KEY]

            // PassRepository.findById is a blocking Room query (see PassDao.findById), hence
            // Dispatchers.IO here.
            val localizedPass =
                passId?.let {
                    withContext(Dispatchers.IO) { passRepository.findById(it) }
                }

            if (localizedPass != null) {
                PassCardGlance(
                    localizedPass = localizedPass,
                    context = context,
                    onClick = {
                        // TODO: e.g. actionStartActivity<MainActivity>(...) to open the pass
                        // detail screen on tap.
                    },
                )
            } else {
                UnconfiguredWidgetContent()
            }
        }
    }
}

@Composable
private fun UnconfiguredWidgetContent() {
    Box(modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
        // TODO: localize via stringResource(R.string....)
        GlanceText(text = "Kein Pass ausgewählt – Widget neu platzieren zum Konfigurieren")
    }
}

class PassCardGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PassCardGlanceWidget()
}

/** Same pattern as WalletViewModel: Hilt @Inject constructor, PassRepository.all() mapped to LocalizedPassWithTags. */
@HiltViewModel
class PassCardWidgetConfigViewModel
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

/** Started by Android when the user drags the widget onto the home screen (android:configure in passcard_widget_info.xml). */
@AndroidEntryPoint
class PassCardWidgetConfigActivity : ComponentActivity() {

    private val viewModel: PassCardWidgetConfigViewModel by viewModels()

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

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
                GlanceAppWidgetManager(this@PassCardWidgetConfigActivity)
                    .getGlanceIdBy(appWidgetId)

            updateAppWidgetState(this@PassCardWidgetConfigActivity, glanceId) { prefs ->
                prefs[PassCardWidgetPrefs.PASS_ID_KEY] = pass.pass.id
            }
            PassCardGlanceWidget().update(this@PassCardWidgetConfigActivity, glanceId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
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
                headlineContent = { Text(pass.pass.description) },
                modifier = Modifier.clickable { onPassSelected(pass) },
            )
        }
    }
}

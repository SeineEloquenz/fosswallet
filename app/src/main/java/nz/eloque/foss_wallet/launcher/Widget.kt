package nz.eloque.foss_wallet.launcher

import android.app.Activity
import android.appwidget.AppWidgetManager
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
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
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.contentprovider.ProviderEntrypoint
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.ui.glance.PassCardGlance
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import java.util.Locale

object PassCardWidgetPrefs {
    val PASS_ID_KEY = stringPreferencesKey("pass_id")
}

// Glance writes ActionParameters as Intent extras under the same name.
private val PASS_ID_PARAM = ActionParameters.Key<String>(MainActivity.EXTRA_PASS_ID)

// No constructor parameter for PassRepository: GlanceAppWidgetReceiver.glanceAppWidget is a
// getter without a Context, so it's resolved here in provideGlance() via ProviderEntrypoint
// instead (same pattern as CatimaContentProvider.kt).
class Widget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(
        context: android.content.Context,
        id: GlanceId,
    ) {
        val passRepository =
            EntryPointAccessors
                .fromApplication(context.applicationContext, ProviderEntrypoint::class.java)
                .passRepository()

        provideContent {
            val prefs = currentState<Preferences>()
            val passId = prefs[PassCardWidgetPrefs.PASS_ID_KEY]
            val localizedPass = passId?.let { withContext(Dispatchers.IO) { passRepository.findById(it) } }

            if (localizedPass != null) {
                PassCardGlance(
                    localizedPass = localizedPass,
                    context = context,
                    onClick =
                        actionStartActivity<MainActivity>(
                            parameters = actionParametersOf(PASS_ID_PARAM to localizedPass.pass.id),
                        ),
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
        GlanceText(text = stringResource(R.string.widget_unconfigured))
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
                GlanceAppWidgetManager(this@WidgetConfigActivity)
                    .getGlanceIdBy(appWidgetId)

            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { prefs ->
                prefs[PassCardWidgetPrefs.PASS_ID_KEY] = pass.pass.id
            }
            Widget().update(this@WidgetConfigActivity, glanceId)

            val resultValue = android.content.Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
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

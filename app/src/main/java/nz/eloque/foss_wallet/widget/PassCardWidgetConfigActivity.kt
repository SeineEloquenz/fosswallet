package nz.eloque.foss_wallet.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import nz.eloque.foss_wallet.ui.theme.WalletTheme
import java.util.Locale

/**
 * ViewModel folgt demselben Muster wie WalletViewModel (Hilt @Inject-Konstruktor,
 * PassRepository.all() als Flow<List<PassWithMetadata>>, hier zusätzlich über
 * applyLocalization() in LocalizedPassWithTags umgewandelt, weil das Widget genau diesen Typ
 * für PassCardGlance() braucht).
 */
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

/**
 * Wird von Android automatisch gestartet, wenn der Nutzer das Widget auf den Homescreen zieht
 * (siehe android:configure in res/xml/passcard_widget_info.xml).
 */
@AndroidEntryPoint
class PassCardWidgetConfigActivity : ComponentActivity() {

    private val viewModel: PassCardWidgetConfigViewModel by viewModels()

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Falls der Nutzer abbricht (z. B. Back-Taste), soll das Widget NICHT platziert werden.
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

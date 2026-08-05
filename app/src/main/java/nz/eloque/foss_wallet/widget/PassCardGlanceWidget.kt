package nz.eloque.foss_wallet.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.contentprovider.ProviderEntrypoint
import nz.eloque.foss_wallet.ui.card.glance.PassCardGlance

object PassCardWidgetPrefs {
    val PASS_ID_KEY = stringPreferencesKey("pass_id")
}

/**
 * Zeigt genau einen, pro Widget-Instanz konfigurierbaren Pass an (2:1, showEntirePass=false).
 *
 * Größe: fest aus res/xml/passcard_widget_info.xml (resizeMode="none"), damit bleibt die
 * 2:1-Ratio garantiert erhalten.
 *
 * Welcher Pass angezeigt wird, steht pro Widget-Instanz (GlanceId) im Preferences-DataStore
 * unter PassCardWidgetPrefs.PASS_ID_KEY (gesetzt in PassCardWidgetConfigActivity).
 *
 * Kein Konstruktor-Parameter für PassRepository: GlanceAppWidgetReceiver.glanceAppWidget ist
 * ein Getter OHNE Context (BroadcastReceiver hält keinen Context als Property), daher kann das
 * Repository nicht dort injiziert werden. Stattdessen wird es hier in provideGlance() aufgelöst,
 * wo `context` als Parameter vorliegt - über dasselbe ProviderEntrypoint-Hilt-Interface, das
 * CatimaContentProvider.kt im Projekt bereits für denselben Zweck nutzt.
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

            // PassRepository.findById(id) ist eine blockierende Room-Query (kein suspend fun,
            // siehe PassDao.findById), deshalb explizit auf Dispatchers.IO auslagern.
            val localizedPass =
                passId?.let {
                    withContext(Dispatchers.IO) { passRepository.findById(it) }
                }

            if (localizedPass != null) {
                PassCardGlance(
                    localizedPass = localizedPass,
                    context = context,
                    onClick = {
                        // TODO: z. B. actionStartActivity<MainActivity>(
                        //     actionParametersOf(PassIdKey to localizedPass.pass.id)
                        // ), um beim Tap direkt zum Pass in der App zu springen.
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
        // TODO: ggf. über stringResource(R.string....) lokalisieren.
        Text(text = "Kein Pass ausgewählt – Widget neu platzieren zum Konfigurieren")
    }
}

package nz.eloque.foss_wallet.ui

import android.content.res.Resources
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.api.ImportOutcome
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel

/** Shows the outcome of every import started through [walletViewModel] and opens a single imported pass. */
@Composable
fun ImportEventsEffect(
    walletViewModel: WalletViewModel,
    onOpenPass: (passId: String) -> Unit,
    onImported: () -> Unit = {},
) {
    val context = LocalContext.current
    var fallbackWarning by remember { mutableStateOf<Pair<Set<String>, CompletableDeferred<Unit>>?>(null) }
    LaunchedEffect(walletViewModel) {
        walletViewModel.importEvents.collect { event ->
            val outcome = event.outcome
            Toast.makeText(context, outcome.message(context.resources), outcome.toastDuration()).show()
            if (outcome.barcodeFallbacks.isNotEmpty()) {
                val acknowledged = CompletableDeferred<Unit>()
                fallbackWarning = outcome.barcodeFallbacks to acknowledged
                acknowledged.await()
            }
            if (outcome is ImportOutcome.Single || outcome is ImportOutcome.Multiple) onImported()
            if (event.openPass && outcome is ImportOutcome.Single) onOpenPass(outcome.passId)
        }
    }
    fallbackWarning?.let { (formats, acknowledged) ->
        BarcodeFallbackDialog(formats, onConfirm = {
            fallbackWarning = null
            acknowledged.complete(Unit)
        })
    }
}

fun ImportOutcome.message(resources: Resources): String =
    when (this) {
        is ImportOutcome.Single -> {
            when (result) {
                ImportResult.Replaced -> resources.getString(R.string.pass_already_imported)
                ImportResult.AutoArchived -> resources.getString(R.string.pass_imported_into_the_archive)
                ImportResult.New -> resources.getString(R.string.pass_imported)
            }
        }

        is ImportOutcome.Multiple -> {
            resources.getString(R.string.n_passes_imported, count)
        }

        ImportOutcome.Invalid -> {
            resources.getString(R.string.invalid_pass_toast)
        }

        ImportOutcome.Empty -> {
            resources.getString(R.string.no_passes_found_in_file)
        }
    }

private fun ImportOutcome.toastDuration(): Int =
    if (this is ImportOutcome.Multiple || (this is ImportOutcome.Single && result == ImportResult.New)) {
        Toast.LENGTH_SHORT
    } else {
        Toast.LENGTH_LONG
    }

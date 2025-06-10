package nz.eloque.foss_wallet.ui.view.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.uiState.collectAsState()

    Column {
        SettingsSwitch(
            name = R.string.enable_sync,
            switchState = settings.value.enableSync,
            onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enableSync(it) } }
        )
        SubmittableTextField(
            label = { Text(stringResource(R.string.sync_interval)) },
            initialValue = settings.value.syncInterval.inWholeMinutes.toString(),
            imageVector = Icons.Default.Save,
            inputValidator = { isInt(it) },
            onSubmit = {
                coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setSyncInterval(Integer.parseInt(it).toDuration(
                    DurationUnit.MINUTES)) }
            },
            enabled = settings.value.enableSync,
            clearOnSubmit = false,
        )
    }
}

private fun isInt(value: String): Boolean {
    return try {
        Integer.parseInt(value)
        true
    } catch (_: NumberFormatException) {
        false
    }
}

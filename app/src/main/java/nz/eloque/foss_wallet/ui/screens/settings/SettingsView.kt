package nz.eloque.foss_wallet.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.share.share
import nz.eloque.foss_wallet.ui.components.ComboBox
import nz.eloque.foss_wallet.ui.screens.wallet.PassViewModel
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    passViewModel: PassViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.uiState.collectAsState()
    val passes = passViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(
            heading = stringResource(R.string.pass_updates_channel),
        ) {
            SettingsSwitch(
                name = R.string.enable,
                switchState = settings.value.enableSync,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enableSync(it) } }
            )
            HorizontalDivider()
            SubmittableTextField(
                label = { Text(stringResource(R.string.sync_interval)) },
                initialValue = settings.value.syncInterval.inWholeMinutes.toString(),
                imageVector = Icons.Default.Save,
                inputValidator = { isNaturalNumber(it) },
                onSubmit = {
                    coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setSyncInterval(Integer.parseInt(it).toDuration(
                        DurationUnit.MINUTES)) }
                },
                enabled = settings.value.enableSync,
                clearOnSubmit = false,
            )
        }
        SettingsSection(
            heading = stringResource(R.string.pass_view),
        ) {
            SettingsSwitch(
                name = R.string.pass_view_brightness,
                switchState = settings.value.increasePassViewBrightness,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enablePassViewBrightness(it) } }
            )
            HorizontalDivider()
            ComboBox(
                name = stringResource(R.string.barcode_position),
                options = BarcodePosition.all(),
                selectedOption = settings.value.barcodePosition,
                onOptionSelected = {
                    coroutineScope.launch(Dispatchers.IO) {
                        settingsViewModel.setBarcodePosition(
                            it
                        )
                    }
                },
                optionLabel = { context.getString(it.label) }
            )
        }
        SettingsSection(
            heading = stringResource(R.string.export),
        ) {
            SettingsButton(
                name = stringResource(R.string.export) + " (.pkpasses)",
                icon = Icons.Default.Share,
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        share(passViewModel.uiState.value.passes, context)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.imePadding())
    }
}

@Composable
fun SettingsSection(
    heading: String,
    content: @Composable () -> Unit,
) {
    Text(
        text = heading
    )
    ElevatedCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

private fun isNaturalNumber(value: String): Boolean {
    return try {
        val representation = Integer.parseInt(value)
        representation > 0
    } catch (_: NumberFormatException) {
        false
    }
}

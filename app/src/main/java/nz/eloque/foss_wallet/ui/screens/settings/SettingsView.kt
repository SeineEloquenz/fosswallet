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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.share.share
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.uiState.collectAsState()
    val passFlow = settingsViewModel.passFlow
    val passes by remember(passFlow) { passFlow }.map { it }.collectAsState(listOf())

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { settingsViewModel.refresh() }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(
            heading = stringResource(R.string.pass_updates_channel),
        ) {
            SettingsSwitch(
                title = stringResource(R.string.enable),
                checked = settings.value.enableSync,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enableSync(it) } }
            )
            HorizontalDivider()
            SubmittableTextField(
                label = stringResource(R.string.sync_interval),
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
                title = stringResource(R.string.pass_view_brightness),
                checked = settings.value.increasePassViewBrightness,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enablePassViewBrightness(it) } }
            )
            HorizontalDivider()
            ComboBox(
                title = stringResource(R.string.barcode_position),
                options = BarcodePosition.all(),
                selectedOption = settings.value.barcodePosition,
                onOptionSelected = {
                    coroutineScope.launch(Dispatchers.IO) {
                        settingsViewModel.setBarcodePosition(
                            it
                        )
                    }
                },
                optionLabel = { resources.getString(it.label) }
            )
        }
        SettingsSection(
            heading = stringResource(R.string.delete),
        ) {
            SettingsSwitch(
                title = stringResource(R.string.ask_before_delete),
                checked = settings.value.askBeforeDelete,
                onCheckedChange = {
                    coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setAskBeforeDelete(it) }
                }
            )
        }
        SettingsSection(
            heading = stringResource(R.string.export) + " / " + stringResource(R.string.share_passes),
        ) {
            SettingsButton(
                title = stringResource(R.string.export) + " (.pkpasses)",
                icon = Icons.Default.Share,
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        share(passes.map { it.pass }, context)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
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

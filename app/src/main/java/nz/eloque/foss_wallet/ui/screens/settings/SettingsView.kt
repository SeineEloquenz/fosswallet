package nz.eloque.foss_wallet.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.withContext
import nz.eloque.compose_kit.components.Section
import nz.eloque.compose_kit.input.ComboBox
import nz.eloque.compose_kit.input.SubmittableTextField
import nz.eloque.compose_kit.settings.SettingsButton
import nz.eloque.compose_kit.settings.SettingsSwitch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.backup.BackupFormat
import nz.eloque.foss_wallet.persistence.backup.BackupImportResult
import nz.eloque.foss_wallet.share.share
import java.time.LocalDate
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.uiState.collectAsState()
    val passFlow = settingsViewModel.passFlow
    val passes by remember(passFlow) { passFlow.map { it } }.collectAsState(listOf())
    var backupRunning by remember { mutableStateOf(false) }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            if (uri != null) {
                backupRunning = true
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { settingsViewModel.exportBackup(it) }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, resources.getString(R.string.backup_created), Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        backupRunning = false
                    }
                }
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                backupRunning = true
                coroutineScope.launch(Dispatchers.IO) {
                    val result =
                        context.contentResolver.openInputStream(uri)?.use { settingsViewModel.importBackup(it) }
                            ?: BackupImportResult.Invalid
                    withContext(Dispatchers.Main) {
                        val message =
                            when (result) {
                                is BackupImportResult.Success -> {
                                    resources.getString(R.string.backup_restore_result, result.imported, result.skipped)
                                }

                                BackupImportResult.Invalid -> {
                                    resources.getString(R.string.backup_invalid)
                                }
                            }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                    backupRunning = false
                }
            }
        }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { settingsViewModel.refresh() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding(),
        ) {
            Section(
                heading = stringResource(R.string.pass_updates_channel),
            ) {
                SettingsSwitch(
                    title = stringResource(R.string.enable),
                    checked = settings.value.enableSync,
                    onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enableSync(it) } },
                )
                HorizontalDivider()
                SubmittableTextField(
                    label = stringResource(R.string.sync_interval),
                    initialValue =
                        settings.value.syncInterval.inWholeMinutes
                            .toString(),
                    imageVector = Icons.Default.Save,
                    inputValidator = { isNaturalNumber(it) },
                    onSubmit = {
                        coroutineScope.launch(Dispatchers.IO) {
                            settingsViewModel.setSyncInterval(
                                Integer.parseInt(it).toDuration(DurationUnit.MINUTES),
                            )
                        }
                    },
                    enabled = settings.value.enableSync,
                    clearOnSubmit = false,
                )
            }
            Section(
                heading = stringResource(R.string.pass_view),
            ) {
                SettingsSwitch(
                    title = stringResource(R.string.pass_view_brightness),
                    checked = settings.value.increasePassViewBrightness,
                    onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enablePassViewBrightness(it) } },
                )
                HorizontalDivider()
                ComboBox(
                    title = stringResource(R.string.barcode_position),
                    options = BarcodePosition.all(),
                    selectedOption = settings.value.barcodePosition,
                    onOptionSelected = {
                        coroutineScope.launch(Dispatchers.IO) {
                            settingsViewModel.setBarcodePosition(
                                it,
                            )
                        }
                    },
                    optionLabel = { resources.getString(it.label) },
                )
            }
            Section(
                heading = stringResource(R.string.delete),
            ) {
                SettingsSwitch(
                    title = stringResource(R.string.ask_before_delete),
                    checked = settings.value.askBeforeDelete,
                    onCheckedChange = {
                        coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setAskBeforeDelete(it) }
                    },
                )
            }
            Section(
                heading = stringResource(R.string.backup),
            ) {
                SettingsButton(
                    title = stringResource(R.string.backup_create) + " (.${BackupFormat.FILE_EXTENSION})",
                    icon = Icons.Default.Save,
                    onClick = { exportLauncher.launch("fosswallet-backup-${LocalDate.now()}.${BackupFormat.FILE_EXTENSION}") },
                )
                HorizontalDivider()
                SettingsButton(
                    title = stringResource(R.string.backup_restore),
                    icon = Icons.Default.Restore,
                    onClick = { importLauncher.launch(arrayOf("application/octet-stream", "application/zip", "*/*")) },
                )
                Text(
                    text = stringResource(R.string.backup_hint),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
            Section(
                heading = stringResource(R.string.export) + " / " + stringResource(R.string.share_passes),
            ) {
                SettingsButton(
                    title = stringResource(R.string.export) + " (.pkpasses)",
                    icon = Icons.Default.Share,
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            share(passes.map { it.pass }, context)
                        }
                    },
                )
                Text(
                    text = stringResource(R.string.share_hint),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(modifier = Modifier.imePadding())
        }
        if (backupRunning) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun isNaturalNumber(value: String): Boolean =
    try {
        val representation = Integer.parseInt(value)
        representation > 0
    } catch (_: NumberFormatException) {
        false
    }

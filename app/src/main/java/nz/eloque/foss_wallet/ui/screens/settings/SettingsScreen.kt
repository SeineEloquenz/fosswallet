package nz.eloque.foss_wallet.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.WalletScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    settingsViewModel: SettingsViewModel,
) {
    WalletScaffold(
        onBack = onBack,
        actions = {
            IconButton(onClick = onOpenAbout) {
                Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(R.string.about))
            }
        },
        title = stringResource(id = R.string.settings),
    ) {
        SettingsView(settingsViewModel)
    }
}

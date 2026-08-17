package nz.eloque.foss_wallet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.launcher.LauncherService
import nz.eloque.foss_wallet.model.Pass

@Composable
fun LauncherButton(pass: Pass) {
    val context = LocalContext.current
    val launcherService = remember { LauncherService(context) }

    IconButton(
        onClick = {
            launcherService.createShortcut(pass)
        },
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.AddToHomeScreen,
            contentDescription = stringResource(R.string.add_to_launcher),
        )
    }
}

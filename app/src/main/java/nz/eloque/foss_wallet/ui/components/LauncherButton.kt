package nz.eloque.foss_wallet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.launcher.LauncherService
import nz.eloque.foss_wallet.model.Pass

@Composable
fun LauncherButton(pass: Pass) {
    val context = LocalContext.current
    val launcherService = remember { LauncherService(context) }
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.AddToHomeScreen,
            contentDescription = stringResource(R.string.add_to_launcher),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_shortcut)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.AddToHomeScreen,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    launcherService.createShortcut(pass)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_card_widget)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Widgets,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    launcherService.createWidget(pass, showBarcode = false)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_barcode_widget)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                    )
                },
                onClick = {
                    expanded = false
                    launcherService.createWidget(pass, showBarcode = true)
                },
            )
        }
    }
}

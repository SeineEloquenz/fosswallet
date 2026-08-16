package nz.eloque.foss_wallet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomescreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.launcher.LauncherService
import nz.eloque.foss_wallet.model.Pass
import java.io.File

@Composable
fun LauncherButton(pass: Pass) {
    private val launcherService: LauncherService

    IconButton(
        onClick = {
            launcherService.create(pass)
        }
    ) {
        Icon(
          imageVector = Icons.Default.AddToHomescreen,
          contentDescription = stringResource(R.string.add_to_launcher),
        )
    }
}

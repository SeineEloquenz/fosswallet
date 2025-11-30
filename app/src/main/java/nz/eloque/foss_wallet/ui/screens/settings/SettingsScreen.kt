package nz.eloque.foss_wallet.ui.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
) {
    WalletScaffold(
        navController = navController,
        toolWindow = true,
        actions = {
            IconButton(onClick = {
                navController.navigate(Screen.About.route)
            }) {
                Icon(imageVector = Screen.About.icon, contentDescription = stringResource(R.string.about))
            }
        },
        title = stringResource(id = R.string.settings)
    ) {
        SettingsView(settingsViewModel)
    }
}
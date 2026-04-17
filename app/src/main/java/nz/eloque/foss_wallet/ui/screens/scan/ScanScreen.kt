package nz.eloque.foss_wallet.ui.screens.scan

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavHostController) {
    WalletScaffold(
        navController = navController,
        toolWindow = true,
        title = stringResource(id = Screen.Create.resourceId),
    ) {
        ScanView(
            navController,
        )
    }
}

package nz.eloque.foss_wallet.ui.screens.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    navController: NavHostController,
    createViewModel: CreateViewModel,
    passId: String? = null,
) {
    WalletScaffold(
        navController = navController,
        toolWindow = true,
        title = stringResource(if (passId == null) Screen.Create.resourceId else R.string.edit_pass)
    ) {
        CreateView(navController, createViewModel, passId)
    }
}

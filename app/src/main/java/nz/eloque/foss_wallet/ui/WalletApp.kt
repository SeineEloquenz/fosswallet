@file:OptIn(ExperimentalMaterial3Api::class)

package nz.eloque.foss_wallet.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.screens.AboutScreen
import nz.eloque.foss_wallet.ui.screens.PassScreen
import nz.eloque.foss_wallet.ui.screens.WalletScreen
import nz.eloque.foss_wallet.ui.wallet.PassViewModel

sealed class Screen(val route: String, val icon: ImageVector, @StringRes val resourceId: Int) {
    data object Wallet : Screen("wallet", Icons.Default.Wallet, R.string.wallet)
    data object About : Screen("about", Icons.Default.Info, R.string.about)
}

@Composable
fun WalletApp(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    passViewModel: PassViewModel = viewModel(),
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Wallet.route,
        ) {
            composable(Screen.Wallet.route) {
                WalletScreen(navController, passViewModel)
            }
            composable(Screen.About.route) {
                AboutScreen(navController)
            }
            composable(
                route = "pass/{passId}",
                arguments = listOf(navArgument("passId") { type = NavType.StringType })
            ) { backStackEntry ->
                val passId = backStackEntry.arguments?.getString("passId")!!
                PassScreen(passId, navController, passViewModel)
            }
        }
    }
}

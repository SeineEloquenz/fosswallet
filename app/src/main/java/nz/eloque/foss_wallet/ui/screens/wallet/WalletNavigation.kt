package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.navigation.NavController
import nz.eloque.foss_wallet.ui.Screen

const val OPEN_FAB_MENU_REQUEST = "open_fab_menu_request"

fun NavController.openWalletFabMenu() {
    getBackStackEntry(Screen.Wallet.route).savedStateHandle[OPEN_FAB_MENU_REQUEST] = true
    popBackStack(Screen.Wallet.route, inclusive = false)
}

package nz.eloque.foss_wallet.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import nz.eloque.foss_wallet.ui.wallet.PassViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            PassViewModel(walletApplication().container.passRepository, walletApplication().container.localizationRepository)
        }
    }
}

fun CreationExtras.walletApplication(): WalletApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WalletApplication)

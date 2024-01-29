package nz.eloque.foss_wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import nz.eloque.foss_wallet.ui.WalletApp
import nz.eloque.foss_wallet.ui.theme.WalletTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletTheme {
                WalletApp(
                    this
                )
            }
        }
    }
}

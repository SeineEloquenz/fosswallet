package nz.eloque.foss_wallet.app

import android.app.Application

class WalletApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}

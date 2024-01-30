package nz.eloque.foss_wallet.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import nz.eloque.foss_wallet.persistence.DbPassRepository
import nz.eloque.foss_wallet.persistence.PassRepository
import nz.eloque.foss_wallet.persistence.WalletDb

interface AppContainer {
    val prefs: SharedPreferences
    val passRepository: PassRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override val passRepository: PassRepository by lazy {
        DbPassRepository(WalletDb.getDb(context).passDao())
    }
}

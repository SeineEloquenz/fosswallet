package nz.eloque.foss_wallet.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import nz.eloque.foss_wallet.persistence.WalletDb
import nz.eloque.foss_wallet.persistence.localization.OfflinePassLocalizationRepository
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.OfflinePassRepository
import nz.eloque.foss_wallet.persistence.pass.PassRepository

interface AppContainer {
    val prefs: SharedPreferences
    val passRepository: PassRepository
    val localizationRepository: PassLocalizationRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    override val passRepository: PassRepository by lazy {
        OfflinePassRepository(context, WalletDb.getDb(context).passDao())
    }

    override val localizationRepository: PassLocalizationRepository by lazy {
        OfflinePassLocalizationRepository(WalletDb.getDb(context).localizationDao())
    }
}

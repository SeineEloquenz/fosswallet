package nz.eloque.foss_wallet.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

interface AppContainer {
    val prefs: SharedPreferences
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
}

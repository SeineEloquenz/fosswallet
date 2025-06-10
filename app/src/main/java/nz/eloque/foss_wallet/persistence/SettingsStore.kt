package nz.eloque.foss_wallet.persistence

import android.content.SharedPreferences
import androidx.core.content.edit
import jakarta.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SettingsStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun isSyncEnabled(): Boolean = prefs.getBoolean("syncEnabled", false)

    fun enableSync(enabled: Boolean) = prefs.edit { putBoolean("syncEnabled", enabled) }

    fun syncInterval(): Duration {
        val amount = prefs.getLong("syncInterval", 60)
        return amount.toDuration(DurationUnit.MINUTES)
    }

    fun setSyncInterval(duration: Duration) = prefs.edit {
        putLong("syncInterval", duration.toLong(DurationUnit.MINUTES))
    }
}
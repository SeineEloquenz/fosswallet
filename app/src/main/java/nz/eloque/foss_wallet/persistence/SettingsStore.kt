package nz.eloque.foss_wallet.persistence

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.core.content.edit
import jakarta.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val SYNC_INTERVAL = "syncInterval"
private const val SYNC_ENABLED = "syncEnabled"
private const val BARCODE_POSITION = "barcodePosition"

sealed class BarcodePosition(val arrangement: Arrangement.Vertical, val key: String) {
    object Top : BarcodePosition(Arrangement.Top, "TOP")
    object Center : BarcodePosition(Arrangement.Center, "CENTER")

    companion object {
        fun of(representation: String): BarcodePosition {
            return when (representation) {
                Top.key -> Top
                Center.key -> Center
                else -> Center
            }
        }
    }
}

class SettingsStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun isSyncEnabled(): Boolean = prefs.getBoolean(SYNC_ENABLED, false)

    fun enableSync(enabled: Boolean) = prefs.edit { putBoolean(SYNC_ENABLED, enabled) }

    fun syncInterval(): Duration {
        val amount = prefs.getLong(SYNC_INTERVAL, 60)
        return amount.toDuration(DurationUnit.MINUTES)
    }

    fun setSyncInterval(duration: Duration) = prefs.edit {
        putLong(SYNC_INTERVAL, duration.toLong(DurationUnit.MINUTES))
    }

    fun barcodePosition(): BarcodePosition = BarcodePosition.of(prefs.getString(BARCODE_POSITION, BarcodePosition.Center.key)!!)

    fun setBarcodePosition(barcodePosition: BarcodePosition) = prefs.edit { putString(BARCODE_POSITION, barcodePosition.key) }
}
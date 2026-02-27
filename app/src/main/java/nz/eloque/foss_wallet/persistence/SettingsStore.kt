package nz.eloque.foss_wallet.persistence

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.core.content.edit
import jakarta.inject.Inject
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.SortOptionSerializer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val SYNC_INTERVAL = "syncInterval"
private const val SYNC_ENABLED = "syncEnabled"
private const val BARCODE_POSITION = "barcodePosition"
private const val PASS_VIEW_BRIGHTNESS = "passViewBrightness"
private const val SORT_OPTION = "walletViewSortOption"
private const val DELETE_CONFIRMATION_ENABLED = "deleteConfirmationEnabled"

sealed class BarcodePosition(val arrangement: Arrangement.Vertical, val key: String, @param:StringRes val label: Int) {
    object Top : BarcodePosition(Arrangement.Top, "TOP", R.string.barcode_position_top)
    object Center : BarcodePosition(Arrangement.Center, "CENTER", R.string.barcode_position_center)
    object Bottom : BarcodePosition(Arrangement.Bottom, "BOTTOM", R.string.barcode_position_bottom)

    companion object {
        fun all(): List<BarcodePosition> = listOf(Top, Center, Bottom)
        fun of(representation: String): BarcodePosition {
            return when (representation) {
                Top.key -> Top
                Center.key -> Center
                Bottom.key -> Bottom
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

    fun increasePassViewBrightness(): Boolean = prefs.getBoolean(PASS_VIEW_BRIGHTNESS, false)

    fun enablePassViewBrightness(enabled: Boolean) = prefs.edit { putBoolean(PASS_VIEW_BRIGHTNESS, enabled) }

    fun syncInterval(): Duration {
        val amount = prefs.getLong(SYNC_INTERVAL, 60)
        return amount.toDuration(DurationUnit.MINUTES)
    }

    fun setSyncInterval(duration: Duration) = prefs.edit {
        putLong(SYNC_INTERVAL, duration.toLong(DurationUnit.MINUTES))
    }

    fun barcodePosition(): BarcodePosition = BarcodePosition.of(prefs.getString(BARCODE_POSITION, BarcodePosition.Center.key)!!)

    fun setBarcodePosition(barcodePosition: BarcodePosition) = prefs.edit { putString(BARCODE_POSITION, barcodePosition.key) }

    fun sortOption(): SortOption = SortOptionSerializer.deserialize(prefs.getString(SORT_OPTION, "")!!) ?: SortOption.TimeAdded

    fun setSortOption(sortOption: SortOption) = prefs.edit { putString(SORT_OPTION, SortOptionSerializer.serialize(sortOption)) }

    fun deleteConfirmationEnabled(): Boolean = prefs.getBoolean(DELETE_CONFIRMATION_ENABLED, true)

    fun setDeleteConfirmationEnabled(enabled: Boolean) = prefs.edit { putBoolean(DELETE_CONFIRMATION_ENABLED, enabled) }
}

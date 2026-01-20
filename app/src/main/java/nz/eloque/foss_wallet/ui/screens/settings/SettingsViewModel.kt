package nz.eloque.foss_wallet.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.UpdateScheduler
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class SettingsUiState(
    val enableSync: Boolean = false,
    val syncInterval: Duration = 1.toDuration(DurationUnit.HOURS),
    val barcodePosition: BarcodePosition = BarcodePosition.Center,
    val increasePassViewBrightness: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsStore: SettingsStore,
    private val passStore: PassStore,
    private val updateScheduler: UpdateScheduler,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    val passFlow = passStore.allPasses()

    init {
        update()
    }

    private fun update() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                enableSync = settingsStore.isSyncEnabled(),
                syncInterval = settingsStore.syncInterval(),
                barcodePosition = settingsStore.barcodePosition(),
                increasePassViewBrightness = settingsStore.increasePassViewBrightness(),
            )
        }
    }

    fun enableSync(enabled: Boolean) {
        settingsStore.enableSync(enabled)
        if (enabled) {
            updateScheduler.enableSync()
        } else {
            updateScheduler.disableSync()
        }
        update()
    }
    fun setSyncInterval(duration: Duration) {
        settingsStore.setSyncInterval(duration)
        updateScheduler.updateSyncInterval()
        update()
    }

    fun setBarcodePosition(barcodePosition: BarcodePosition) {
        settingsStore.setBarcodePosition(barcodePosition)
        update()
    }

    fun enablePassViewBrightness(enabled: Boolean) {
        settingsStore.enablePassViewBrightness(enabled)
        update()
    }
}
package nz.eloque.foss_wallet.ui.screens.wallet

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult
import java.util.Locale

data class PassUiState(
    var isAuthenticated = false,
    val query: String = "",
    val passes: List<Pass> = ArrayList()
)

@HiltViewModel
class PassViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
    private val settingsStore: SettingsStore,
    private val workManager: WorkManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PassUiState())
    val uiState: StateFlow<PassUiState> = _uiState.asStateFlow()
    private val workData: LiveData<List<WorkInfo>>
    private val observer = { workInfo: List<WorkInfo> -> updatePasses() }

    init {
        updatePasses()
        workData = workManager.getWorkInfosByTagLiveData("update")
        workData.observeForever(observer)
    }

    override fun onCleared() {
        workData.removeObserver(observer)
    }

    private fun updatePasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(passes = passStore.allPasses().first().map { it.applyLocalization(
                Locale.getDefault().language) })
        }
    }

    fun toggleAuthentication() {
        _uiState.value = _uiState.value.copy(isAuthenticated = !_uiState.value.isAuthenticated)
    }

    fun passById(id: String): PassWithLocalization = passStore.passById(id).apply { updatePasses() }

    fun group(passes: Set<Pass>) = passStore.group(passes).apply { updatePasses() }

    fun deleteGroup(groupId: Long) = passStore.deleteGroup(groupId).apply { updatePasses() }

    fun filter(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(query = query, passes = passStore.filtered(query).first().map { it.applyLocalization(Locale.getDefault().language) })
        }
    }

    fun add(loadResult: PassLoadResult): ImportResult = passStore.add(loadResult).apply { updatePasses() }

    suspend fun update(pass: Pass): UpdateResult = passStore.update(pass).apply { updatePasses() }

    fun delete(pass: Pass) = passStore.delete(pass).apply { updatePasses() }

    fun load(context: Context, bytes: ByteArray): ImportResult = passStore.load(context, bytes).apply { updatePasses() }
    fun associate(groupId: Long, passes: Set<Pass>) = passStore.associate(groupId, passes).apply { updatePasses() }
    fun dessociate(pass: Pass, groupId: Long) = passStore.dessociate(pass, groupId).apply { updatePasses() }

    fun archive(pass: Pass) = passStore.archive(pass).apply { updatePasses() }
    fun unarchive(pass: Pass) = passStore.unarchive(pass).apply { updatePasses() }

    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    
    fun showAllPasses(): List<Pass> = uiState.value.passes

    fun showUnhiddenPasses(): List<Pass> = uiState.value.passes.filterNot { pass -> pass.hidden }
}

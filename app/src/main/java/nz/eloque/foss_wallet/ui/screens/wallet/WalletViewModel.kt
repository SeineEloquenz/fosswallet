package nz.eloque.foss_wallet.ui.screens.wallet

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult

data class QueryState(
    val query: String = ""
)

@HiltViewModel
class PassViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val _queryState = MutableStateFlow(QueryState())
    val queryState: StateFlow<QueryState> = _queryState.asStateFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPasses = queryState.flatMapMerge { passStore.filtered(it.query) }

    fun passFlowById(id: String): Flow<PassWithLocalization?> = passStore.passFlowById(id)

    fun group(passes: Set<Pass>) = passStore.group(passes)

    fun deleteGroup(groupId: Long) = passStore.deleteGroup(groupId)

    fun filter(query: String) {
        viewModelScope.launch {
            _queryState.value = _queryState.value.copy(query = query)
        }
    }

    fun add(loadResult: PassLoadResult): ImportResult = passStore.add(loadResult)

    suspend fun update(pass: Pass): UpdateResult = passStore.update(pass)

    fun delete(pass: Pass) = passStore.delete(pass)

    fun load(context: Context, bytes: ByteArray): ImportResult = passStore.load(context, bytes)
    fun associate(groupId: Long, passes: Set<Pass>) = passStore.associate(groupId, passes)
    fun dissociate(pass: Pass, groupId: Long) = passStore.dissociate(pass, groupId)

    fun archive(pass: Pass) = passStore.archive(pass)
    fun unarchive(pass: Pass) = passStore.unarchive(pass)

    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    fun toggleLegacyRendering(pass: Pass) = passStore.toggleLegacyRendering(pass)
}

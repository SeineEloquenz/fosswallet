package nz.eloque.foss_wallet.ui.screens.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.SortOption
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult
import nz.eloque.foss_wallet.persistence.tag.TagRepository

data class QueryState(
    val query: String = ""
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
    private val tagRepository: TagRepository,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val _queryState = MutableStateFlow(QueryState())
    private val queryState: StateFlow<QueryState> = _queryState.asStateFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPasses = queryState.flatMapMerge { passStore.filtered(it.query) }

    val allTags = tagRepository.all()

    private val _sortOptionState: MutableStateFlow<SortOption> = MutableStateFlow(SortOption.TimeAdded)
    val sortOptionState = _sortOptionState.asStateFlow()

    init {
        update()
        viewModelScope.launch(Dispatchers.IO) {
            passStore.archiveExpiredPasses()
        }
    }

    private fun update() {
        viewModelScope.launch {
            _sortOptionState.value = settingsStore.sortOption()
        }
    }

    fun setSortOption(sortOption: SortOption) {
        settingsStore.setSortOption(sortOption)
        update()
    }

    fun group(passes: Set<Pass>) = passStore.group(passes)

    fun deleteGroup(groupId: Long) = passStore.deleteGroup(groupId)

    fun filter(query: String) {
        viewModelScope.launch {
            _queryState.value = _queryState.value.copy(query = query)
        }
    }

    fun add(loadResult: PassLoadResult): ImportResult = passStore.add(loadResult)

    suspend fun addTag(tag: Tag) = tagRepository.insert(tag)

    suspend fun removeTag(tag: Tag) = tagRepository.remove(tag)

    fun delete(pass: Pass) = passStore.delete(pass)

    fun associate(groupId: Long, passes: Set<Pass>) = passStore.associate(groupId, passes)
    fun dissociate(pass: Pass, groupId: Long) = passStore.dissociate(pass, groupId)

    fun archive(pass: Pass) = passStore.archive(pass)
    fun unarchive(pass: Pass) = passStore.unarchive(pass)

    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
}

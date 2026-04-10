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
    val query: String = "",
)

@HiltViewModel
class WalletViewModel
    @Inject
    constructor(
        application: Application,
        private val passStore: PassStore,
        private val tagRepository: TagRepository,
        val settingsStore: SettingsStore,
    ) : AndroidViewModel(application) {
        private val baseQueryState = MutableStateFlow(QueryState())
        private val queryState: StateFlow<QueryState> = baseQueryState.asStateFlow()
        
        private val _isAuthenticated = MutableStateFlow(false)
        val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

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

        fun group(passes: Set<Pass>) = viewModelScope.launch(Dispatchers.IO) { passStore.group(passes) }

        fun deleteGroup(groupId: Long) = viewModelScope.launch(Dispatchers.IO) { passStore.deleteGroup(groupId) }

        fun filter(query: String) =
            viewModelScope.launch(Dispatchers.IO) { baseQueryState.value = baseQueryState.value.copy(query = query) }

        fun add(loadResult: PassLoadResult): ImportResult = passStore.add(loadResult)

        fun addTag(tag: Tag) = viewModelScope.launch(Dispatchers.IO) { tagRepository.insert(tag) }

        fun removeTag(tag: Tag) = viewModelScope.launch(Dispatchers.IO) { tagRepository.remove(tag) }

        fun delete(pass: Pass) = viewModelScope.launch(Dispatchers.IO) { passStore.delete(pass) }

        fun associate(
            groupId: Long,
            passes: Set<Pass>,
        ) = viewModelScope.launch(Dispatchers.IO) { passStore.associate(groupId, passes) }

        fun dissociate(
            pass: Pass,
            groupId: Long,
        ) = viewModelScope.launch(Dispatchers.IO) { passStore.dissociate(pass, groupId) }

        fun reveal() { _isAuthenticated.value = true }
        
        fun conceal() { _isAuthenticated.value = false }

        fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()
    
        fun archive(pass: Pass) = viewModelScope.launch(Dispatchers.IO) { passStore.archive(pass) }

        fun unarchive(pass: Pass) = viewModelScope.launch(Dispatchers.IO) { passStore.unarchive(pass) }

        fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

        fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    }

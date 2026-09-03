package nz.eloque.foss_wallet.ui.screens.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
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

/** A sorted list of pass groups, keyed by their (nullable) group id. */
typealias GroupedPasses = List<Pair<Long?, List<LocalizedPassWithTags>>>

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

        @OptIn(ExperimentalCoroutinesApi::class)
        private val filteredPasses = queryState.flatMapMerge { passStore.filtered(it.query) }

        val allTags = tagRepository.all()

        private val _sortOptionState: MutableStateFlow<SortOption> = MutableStateFlow(SortOption.TimeAdded)
        val sortOptionState = _sortOptionState.asStateFlow()

        private val _selectedPassTypes = MutableStateFlow(PassType.all().toSet())
        val selectedPassTypes = _selectedPassTypes.asStateFlow()

        private val _tagFilter = MutableStateFlow<Tag?>(null)
        val tagFilter = _tagFilter.asStateFlow()

        /**
         * Fully filtered, sorted and grouped passes, partitioned by their archived state.
         */
        val displayedPasses: StateFlow<Map<Boolean, GroupedPasses>> =
            combine(
                filteredPasses,
                sortOptionState,
                selectedPassTypes,
                tagFilter,
            ) { passes, sortOption, passTypes, tag ->
                passes
                    .filter { localizedPass -> passTypes.any { localizedPass.pass.type.isSameType(it) } }
                    .filter { localizedPass -> tag == null || localizedPass.tags.contains(tag) }
                    .sortedWith(sortOption.comparator)
                    .groupBy { it.metadata.archived }
                    .mapValues { (_, list) -> list.groupBy { it.metadata.groupId }.toList() }
            }.flowOn(Dispatchers.Default)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

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

        fun selectPassType(passType: PassType) {
            _selectedPassTypes.value = _selectedPassTypes.value + passType
        }

        fun deselectPassType(passType: PassType) {
            _selectedPassTypes.value = _selectedPassTypes.value - passType
        }

        fun setTagFilter(tag: Tag?) {
            _tagFilter.value = tag
        }

        fun group(passes: Set<Pass>) = viewModelScope.launch(Dispatchers.IO) { passStore.group(passes) }

        fun deleteGroup(groupId: Long) = viewModelScope.launch(Dispatchers.IO) { passStore.deleteGroup(groupId) }

        fun filter(query: String) =
            viewModelScope.launch(Dispatchers.IO) { baseQueryState.value = baseQueryState.value.copy(query = query) }

        suspend fun add(loadResult: PassLoadResult): ImportResult = passStore.add(loadResult)

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

        fun archive(pass: Pass) = viewModelScope.launch(Dispatchers.IO) { passStore.archive(pass) }

        fun unarchive(pass: Pass) = viewModelScope.launch(Dispatchers.IO) { passStore.unarchive(pass) }

        fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

        fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    }

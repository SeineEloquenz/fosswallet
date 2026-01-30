package nz.eloque.foss_wallet.ui.screens.wallet

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.PassStore
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
) : AndroidViewModel(application) {

    private val _queryState = MutableStateFlow(QueryState())
    private val queryState: StateFlow<QueryState> = _queryState.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPasses = queryState.flatMapMerge { passStore.filtered(it.query) }

    val allTags = tagRepository.all()

    fun group(passes: Set<Pass>) = passStore.group(passes)
    fun deleteGroup(groupId: Long) = passStore.deleteGroup(groupId)

    fun filter(query: String) {
        viewModelScope.launch {
            _queryState.value = _queryState.value.copy(query = query)
        }
    }

    fun add(loadResult: PassLoadResult): ImportResult = passStore.add(loadResult)

    fun load(context: Context, bytes: ByteArray): ImportResult = passStore.load(context, bytes)
    
    fun associate(groupId: Long, passes: Set<Pass>) = passStore.associate(groupId, passes)
    fun dissociate(pass: Pass, groupId: Long) = passStore.dissociate(pass, groupId)

    suspend fun addTag(tag: Tag) = tagRepository.insert(tag)
    suspend fun removeTag(tag: Tag) = tagRepository.remove(tag)
}

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult
import nz.eloque.foss_wallet.persistence.tag.TagRepository

data class QueryState(
    val query: String = "",
    val passes: List<Pass> = ArrayList()
)

@HiltViewModel
class PassViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
    private val tagRepository: TagRepository,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: Boolean = false

    private val _isHidden = MutableStateFlow(false)
    val isHidden: Boolean = false

    private val _isPinned = MutableStateFlow(false)
    val isPinned: Boolean = false
    private val _queryState = MutableStateFlow(QueryState())
    val queryState: StateFlow<QueryState> = _queryState.asStateFlow()
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPasses = queryState.flatMapMerge { passStore.filtered(it.query, it.isAuthenticated) }

    val allTags = tagRepository.all()

    fun passFlowById(id: String) = passStore.passFlowById(id)

    fun group(passes: Set<Pass>) = passStore.group(passes)

    fun deleteGroup(groupId: Long) = passStore.deleteGroup(groupId)

    fun filter(query: String, authStatus: Boolean) {
        viewModelScope.launch {
            _queryState.value = _queryState.value.copy(query = query)
            _isAuthenticated.value = authStatus
        }
    }

    fun add(loadResult: PassLoadResult): ImportResult = passStore.add(loadResult)

    suspend fun addTag(tag: Tag) = tagRepository.insert(tag)

    suspend fun removeTag(tag: Tag) = tagRepository.remove(tag)

    suspend fun tag(pass: Pass, tag: Tag) = passStore.tag(pass, tag)

    suspend fun untag(pass: Pass, tag: Tag) = passStore.untag(pass, tag)

    suspend fun update(pass: Pass): UpdateResult = passStore.update(pass)

    fun delete(pass: Pass) = passStore.delete(pass)

    fun load(context: Context, bytes: ByteArray): ImportResult = passStore.load(context, bytes)
    fun associate(groupId: Long, passes: Set<Pass>) = passStore.associate(groupId, passes)
    fun dissociate(pass: Pass, groupId: Long) = passStore.dissociate(pass, groupId)

    fun archive(pass: Pass) = passStore.archive(pass)
    fun unarchive(pass: Pass) = passStore.unarchive(pass)

    fun hide(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.hide(pass)
        withContext(Dispatchers.Main) {
            _isHidden.value = false
        }
    }
    fun unhide(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.unhide(pass)
        withContext(Dispatchers.Main) {
            _isHidden.value = true
        }
    }
    fun hidden(pass: Pass) { _isHidden.value = passStore.isHidden(pass) }

    fun pin(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.pin(pass)
        withContext(Dispatchers.Main) {
            _isPinned.value = true
        }
    }
    fun unpin(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.unpin(pass)
        withContext(Dispatchers.Main) {
            _isPinned.value = false
        }
    }
    fun pinned(pass: Pass) { _isPinned.value = passStore.isPinned(pass) }

    fun reveal() { _isAuthenticated.value = true }
    fun conceal() { _isAuthenticated.value = false }

    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    fun toggleLegacyRendering(pass: Pass) = passStore.toggleLegacyRendering(pass)
}

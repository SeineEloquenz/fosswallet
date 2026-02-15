package nz.eloque.foss_wallet.ui.screens.pass

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.tag.TagRepository

@HiltViewModel
class PassViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
    private val tagRepository: TagRepository,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _isHidden = MutableStateFlow(false)
    val isHidden: StateFlow<Boolean> = _isHidden.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()
    
    val allTags = tagRepository.all()

    fun passFlowById(id: String) = passStore.passFlowById(id)

    suspend fun addTag(tag: Tag) = tagRepository.insert(tag)

    suspend fun tag(pass: Pass, tag: Tag) = passStore.tag(pass, tag)

    suspend fun untag(pass: Pass, tag: Tag) = passStore.untag(pass, tag)

    suspend fun update(pass: Pass): UpdateResult = passStore.update(pass)

    fun delete(pass: Pass) = passStore.delete(pass)

    fun hide(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.hide(pass)
        withContext(Dispatchers.Main) { _isHidden.value = true }
    }
    fun unhide(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.unhide(pass)
        withContext(Dispatchers.Main) { _isHidden.value = false }
    }
    
    fun pin(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.pin(pass)
        withContext(Dispatchers.Main) { _isPinned.value = true }
    }
    fun unpin(pass: Pass) = viewModelScope.launch(Dispatchers.IO) {
        passStore.unpin(pass)
        withContext(Dispatchers.Main) { _isPinned.value = false }
    }

    fun hidden(pass: Pass) { _isHidden.value = passStore.isHidden(pass) }
    fun pinned(pass: Pass) { _isPinned.value = passStore.isPinned(pass) }

    fun reveal() { _isAuthenticated.value = true }
    fun conceal() { _isAuthenticated.value = false }
    
    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    fun toggleLegacyRendering(pass: Pass) = passStore.toggleLegacyRendering(pass)
}

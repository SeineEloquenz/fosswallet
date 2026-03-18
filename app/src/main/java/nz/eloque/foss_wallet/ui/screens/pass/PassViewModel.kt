package nz.eloque.foss_wallet.ui.screens.pass

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
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
    val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    val allTags = tagRepository.all()

    fun passFlowById(id: String) = passStore.passFlowById(id)

    fun addTag(tag: Tag) = viewModelScope.launch { tagRepository.insert(tag) }

    fun tag(pass: Pass, tag: Tag) = viewModelScope.launch { passStore.tag(pass, tag) }
    fun untag(pass: Pass, tag: Tag) = viewModelScope.launch { passStore.untag(pass, tag) }
    
    fun update(pass: Pass, onResult: (UpdateResult) -> Unit = {}) {
        viewModelScope.launch { onResult.invoke(passStore.update(pass)) }
    }

    fun delete(pass: Pass) = viewModelScope.launch { passStore.delete(pass) }

    fun archive(pass: Pass) = viewModelScope.launch { passStore.archive(pass) }
    fun unarchive(pass: Pass) = viewModelScope.launch { passStore.unarchive(pass) }

    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()

    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    fun toggleLegacyRendering(pass: Pass) = viewModelScope.launch { passStore.toggleLegacyRendering(pass) }
}

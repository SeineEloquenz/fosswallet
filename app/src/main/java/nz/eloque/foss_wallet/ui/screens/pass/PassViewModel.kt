package nz.eloque.foss_wallet.ui.screens.pass

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.ImportResult
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

    val allTags = tagRepository.all()

    fun passFlowById(id: String) = passStore.passFlowById(id)
    fun load(context: Context, bytes: ByteArray): ImportResult = passStore.load(context, bytes)

    fun group(passes: Set<Pass>) = passStore.group(passes)

    fun tag(pass: Pass, tag: Tag) { viewModelScope.launch { passStore.tag(pass, tag) } }
    fun untag(pass: Pass, tag: Tag) { viewModelScope.launch { passStore.untag(pass, tag) } }

    suspend fun update(pass: Pass): UpdateResult = passStore.update(pass)
    fun delete(pass: Pass) = passStore.delete(pass)

    fun archive(pass: Pass) = passStore.archive(pass)
    fun unarchive(pass: Pass) = passStore.unarchive(pass)

    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()
    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    
    fun toggleLegacyRendering(pass: Pass) = passStore.toggleLegacyRendering(pass)
}

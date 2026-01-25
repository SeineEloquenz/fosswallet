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
import nz.eloque.foss_wallet.api.UpdateResult
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult
import nz.eloque.foss_wallet.persistence.tag.TagRepository


@HiltViewModel
class PassViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
    private val tagRepository: TagRepository,
    private val settingsStore: SettingsStore
) : AndroidViewModel(application) {

    fun passFlowById(id: String) = passStore.passFlowById(id)
    fun load(context: Context, bytes: ByteArray): ImportResult = passStore.load(context, bytes)

    suspend fun tag(pass: Pass, tag: Tag) = passStore.tag(pass, tag)
    suspend fun untag(pass: Pass, tag: Tag) = passStore.untag(pass, tag)

    suspend fun update(pass: Pass): UpdateResult = passStore.update(pass)
    fun delete(pass: Pass) = passStore.delete(pass)

    fun associate(groupId: Long, passes: Set<Pass>) = passStore.associate(groupId, passes)
    fun dissociate(pass: Pass, groupId: Long) = passStore.dissociate(pass, groupId)

    fun archive(pass: Pass) = passStore.archive(pass)
    fun unarchive(pass: Pass) = passStore.unarchive(pass)

    fun barcodePosition(): BarcodePosition = settingsStore.barcodePosition()
    fun increasePassViewBrightness(): Boolean = settingsStore.increasePassViewBrightness()
    
    fun toggleLegacyRendering(pass: Pass) = passStore.toggleLegacyRendering(pass)
}

package nz.eloque.foss_wallet.ui.wallet

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.persistence.PassBitmaps
import nz.eloque.foss_wallet.persistence.PassStore
import java.io.InputStream
import java.util.Locale

data class PassUiState(
    val passes: List<Pass> = ArrayList()
)

@HiltViewModel
class PassViewModel @Inject constructor(
    application: Application,
    private val passStore: PassStore,
    private val workManager: WorkManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PassUiState())
    val uiState: StateFlow<PassUiState> = _uiState.asStateFlow()
    private val workData: LiveData<List<WorkInfo>>
    private val observer = { workInfo: List<WorkInfo> -> updatePasses() }

    init {
        updatePasses()
        workData = workManager.getWorkInfosByTagLiveData("update")
        workData.observeForever(observer)
    }

    override fun onCleared() {
        workData.removeObserver(observer)
    }

    private fun updatePasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(passes = passStore.allPasses().first().map { it.applyLocalization(
                Locale.getDefault().language) })
        }
    }

    suspend fun passById(id: Long): PassWithLocalization = passStore.passById(id).apply { updatePasses() }

    suspend fun add(pass: Pass, bitmaps: PassBitmaps, localization: Set<PassLocalization>): Long = passStore.add(pass, bitmaps, localization).apply { updatePasses() }

    suspend fun update(pass: Pass): Pass? = passStore.update(pass).apply { updatePasses() }

    suspend fun delete(pass: Pass) = passStore.delete(pass).apply { updatePasses() }

    suspend fun load(context: Context, inputStream: InputStream) = passStore.load(context, inputStream).apply { updatePasses() }
}

package nz.eloque.foss_wallet.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.PassBitmaps
import nz.eloque.foss_wallet.persistence.PassRepository

data class PassUiState(
    val passes: List<Pass> = ArrayList()
)

class PassViewModel(
    private val passRepository: PassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PassUiState())
    val uiState: StateFlow<PassUiState> = _uiState.asStateFlow()

    init {
        updatePasses()
    }

    private fun updatePasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(passes = passRepository.all().first())
        }
    }

    suspend fun passById(id: Int) = passRepository.byId(id)

    suspend fun add(pass: Pass, bitmaps: PassBitmaps): Long {
        val id = passRepository.insert(Pair(pass, bitmaps))
        updatePasses()
        return id
    }

    suspend fun delete(pass: Pass) {
        passRepository.delete(pass)
        updatePasses()
    }
}

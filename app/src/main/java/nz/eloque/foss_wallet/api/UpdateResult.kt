package nz.eloque.foss_wallet.api

import androidx.annotation.StringRes
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.persistence.PassLoadResult

sealed class UpdateResult() {
    data class Success(val content: UpdateContent) : UpdateResult()
    data class Failed(val reason: FailureReason) : UpdateResult()
}

sealed class UpdateContent() {
    data class LoadResult(val result: PassLoadResult) : UpdateContent()
    data class Pass(val pass: nz.eloque.foss_wallet.model.Pass) : UpdateContent()
}

sealed class FailureReason(@StringRes val messageId: Int, val detailed: Boolean) {
    object Timeout : FailureReason(R.string.timeout, false)
    data class Exception(val exception: kotlin.Exception) : FailureReason(R.string.exception, true)
    data class Status(val status: Int) : FailureReason(R.string.status_code, false)
}
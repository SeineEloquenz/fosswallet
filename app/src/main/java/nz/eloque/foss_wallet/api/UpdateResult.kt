package nz.eloque.foss_wallet.api

import androidx.annotation.StringRes
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.persistence.loader.PassLoadResult

sealed class UpdateResult() {
    data class Success(val content: UpdateContent) : UpdateResult()
    data object NotUpdated : UpdateResult()
    data class Failed(val reason: FailureReason) : UpdateResult()
}

sealed class UpdateContent() {
    data class LoadResult(val result: PassLoadResult) : UpdateContent()
    data class Pass(val pass: nz.eloque.foss_wallet.model.Pass) : UpdateContent()
}

sealed class FailureReason(@param:StringRes val messageId: Int) {
    object Timeout : FailureReason(R.string.timeout)
    data class Exception(val exception: kotlin.Exception) : FailureReason(R.string.exception), Detailed
    data class Status(val status: Int) : FailureReason(R.string.status_code), Detailed
    data object Forbidden : FailureReason(R.string.status_forbidden)
    interface Detailed
}

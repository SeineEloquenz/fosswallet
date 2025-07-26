package nz.eloque.foss_wallet.api

sealed class ImportResult {
    object New : ImportResult()
    object Replaced : ImportResult()
}
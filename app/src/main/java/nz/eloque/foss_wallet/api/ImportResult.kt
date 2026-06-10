package nz.eloque.foss_wallet.api

sealed class ImportResult {
    object AutoArchived : ImportResult()

    object New : ImportResult()

    object Replaced : ImportResult()
}

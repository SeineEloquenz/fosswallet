package nz.eloque.foss_wallet.api

sealed class ImportResult {
    object AutoArchived : ImportResult()

    object New : ImportResult()

    object Replaced : ImportResult()
}

/** The outcome of importing one pkpass or pkpasses file. */
sealed interface ImportOutcome {
    data class Single(
        val passId: String,
        val result: ImportResult,
    ) : ImportOutcome

    data class Multiple(
        val count: Int,
    ) : ImportOutcome

    data object Invalid : ImportOutcome

    data object Empty : ImportOutcome
}

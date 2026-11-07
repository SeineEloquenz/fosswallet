package nz.eloque.foss_wallet.api

sealed class ImportResult {
    object AutoArchived : ImportResult()

    object New : ImportResult()

    object Replaced : ImportResult()
}

/** The outcome of importing one pkpass or pkpasses file. */
sealed interface ImportOutcome {
    /** Barcode formats that could not be rendered and were replaced with QR codes. */
    val barcodeFallbacks: Set<String> get() = emptySet()

    data class Single(
        val passId: String,
        val result: ImportResult,
        override val barcodeFallbacks: Set<String> = emptySet(),
    ) : ImportOutcome

    data class Multiple(
        val count: Int,
        override val barcodeFallbacks: Set<String> = emptySet(),
    ) : ImportOutcome

    data object Invalid : ImportOutcome

    data object Empty : ImportOutcome
}

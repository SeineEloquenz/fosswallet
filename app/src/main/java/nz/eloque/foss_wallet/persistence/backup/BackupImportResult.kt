package nz.eloque.foss_wallet.persistence.backup

/** Outcome of restoring a `.fosswallet` backup. */
sealed class BackupImportResult {
    /** The backup was read; [imported] new passes added, [skipped] already present, [failed] unreadable. */
    data class Success(
        val imported: Int,
        val skipped: Int,
        val failed: Int,
    ) : BackupImportResult()

    /** The input was not a readable `.fosswallet` backup. */
    object Invalid : BackupImportResult()
}

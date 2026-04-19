package nz.eloque.foss_wallet.persistence

interface TransactionalExecutor {
    suspend fun <T> runTransactionally(callable: suspend () -> T): T
}

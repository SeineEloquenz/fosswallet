package nz.eloque.foss_wallet.persistence.migrations

import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL

@Suppress("ClassName")
class M14_15 : AutoMigrationSpec {
    override suspend fun onPostMigrate(connection: SQLiteConnection) {
        connection.executeSQL(
            """
            UPDATE pass
            SET relevantDate = NULL
            WHERE relevantDate = 0
            """.trimIndent(),
        )
        connection.executeSQL(
            """
            UPDATE pass
            SET expirationDate = NULL
            WHERE expirationDate = 0
            """.trimIndent(),
        )
    }
}

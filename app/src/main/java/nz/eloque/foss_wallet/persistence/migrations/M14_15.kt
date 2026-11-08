package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Suppress("ClassName")
class M14_15 : AutoMigrationSpec {
    override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            UPDATE pass
            SET relevantDate = NULL
            WHERE relevantDate = 0
            """.trimIndent(),
        )
        connection.execSQL(
            """
            UPDATE pass
            SET expirationDate = NULL
            WHERE expirationDate = 0
            """.trimIndent(),
        )
    }
}

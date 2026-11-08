package nz.eloque.foss_wallet.persistence.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL

val M_18_19 =
    object : Migration(18, 19) {
        override suspend fun migrate(connection: SQLiteConnection) {
            connection.executeSQL(
                """
                ALTER TABLE Pass
                ADD COLUMN relevantDates TEXT NOT NULL DEFAULT '[]'
                """.trimIndent(),
            )
        }
    }

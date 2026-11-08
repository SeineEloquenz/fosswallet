package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val M_18_19 =
    object : Migration(18, 19) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                ALTER TABLE Pass
                ADD COLUMN relevantDates TEXT NOT NULL DEFAULT '[]'
                """.trimIndent(),
            )
        }
    }

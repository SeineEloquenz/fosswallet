package nz.eloque.foss_wallet.persistence.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

val M_17_18 =
    object : Migration(17, 18) {
        override suspend fun migrate(connection: SQLiteConnection) {
            val rows =
                connection.prepare("SELECT id, relevantDate, expirationDate FROM Pass").use { statement ->
                    buildList {
                        while (statement.step()) {
                            add(Triple(statement.getText(0), statement.getTextOrNull(1), statement.getTextOrNull(2)))
                        }
                    }
                }

            connection
                .prepare(
                    """
                    UPDATE Pass SET
                        relevantDate = ?,
                        expirationDate = ?
                    WHERE id = ?
                    """.trimIndent(),
                ).use { update ->
                    rows.forEach { (id, relevantDateSeconds, expirationDateSeconds) ->
                        update.bindTextOrNull(1, relevantDateSeconds?.let(::isoFromEpochSeconds))
                        update.bindTextOrNull(2, expirationDateSeconds?.let(::isoFromEpochSeconds))
                        update.bindText(3, id)
                        update.step()
                        update.reset()
                    }
                }
        }
    }

private fun isoFromEpochSeconds(seconds: String): String =
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds.toLong()), ZoneOffset.UTC).toString()

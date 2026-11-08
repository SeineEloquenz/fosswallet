package nz.eloque.foss_wallet.persistence.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.persistence.TypeConverters
import java.time.ZonedDateTime

val M_19_20 =
    object : Migration(19, 20) {
        override suspend fun migrate(connection: SQLiteConnection) {
            val converters = TypeConverters()

            val updates =
                connection.prepare("SELECT id, relevantDate, relevantDates FROM Pass").use { statement ->
                    buildList {
                        while (statement.step()) {
                            val relevantDate = statement.getTextOrNull(1)?.let(ZonedDateTime::parse) ?: continue
                            val relevantDates = statement.getTextOrNull(2)?.let { converters.toRelevantDates(it) } ?: listOf()
                            if (relevantDates.isEmpty()) {
                                add(statement.getText(0) to converters.fromRelevantDates(listOf(PassRelevantDate.Date(relevantDate))))
                            }
                        }
                    }
                }

            connection
                .prepare(
                    """
                    UPDATE Pass SET
                        relevantDates = ?
                    WHERE id = ?
                    """.trimIndent(),
                ).use { update ->
                    updates.forEach { (id, relevantDates) ->
                        update.bindText(1, relevantDates)
                        update.bindText(2, id)
                        update.step()
                        update.reset()
                    }
                }
        }
    }

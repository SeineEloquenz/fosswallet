package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.persistence.TypeConverters
import java.time.ZonedDateTime

val M_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {

        val converters = TypeConverters()

        val cursor = db.query("SELECT id, relevantDate, relevantDates FROM Pass")
        while (cursor.moveToNext()) {
            val id = cursor.getString(0)
            val relevantDateString = if (!cursor.isNull(1)) cursor.getString(1) else continue
            val relevantDatesString = if (!cursor.isNull(2)) cursor.getString(2) else null

            val relevantDate = ZonedDateTime.parse(relevantDateString)
            val relevantDates = relevantDatesString?.let { converters.toRelevantDates(it) } ?: listOf()

            if (relevantDates.isEmpty() && relevantDate != null) {
                val withRelevantDate = relevantDates.toMutableList()
                withRelevantDate.add(PassRelevantDate.Date(relevantDate))

                db.execSQL(
                    """
                UPDATE Pass SET
                    relevantDates = ?
                WHERE id = ?
                """.trimIndent(),
                    arrayOf(converters.fromRelevantDates(withRelevantDate), id)
                )
            }
        }
        cursor.close()
    }
}
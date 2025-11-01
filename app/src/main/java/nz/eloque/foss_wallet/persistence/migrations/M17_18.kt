package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

val M_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {

        val cursor = db.query("SELECT id, relevantDate, expirationDate FROM Pass")
        while (cursor.moveToNext()) {
            val id = cursor.getString(0)
            val relevantDateMillis = if (!cursor.isNull(1)) cursor.getString(1) else null
            val expirationDateMillis = if (!cursor.isNull(2)) cursor.getString(2) else null

            val relevantIso = relevantDateMillis?.let {
                ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(it.toLong()),
                    ZoneOffset.UTC
                ).toString()
            }

            val expirationIso = expirationDateMillis?.let {
                ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(it.toLong()),
                    ZoneOffset.UTC
                ).toString()
            }

            db.execSQL(
                """
                UPDATE Pass SET
                    relevantDate = ?,
                    expirationDate = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf(relevantIso, expirationIso, id)
            )
        }
        cursor.close()
    }
}

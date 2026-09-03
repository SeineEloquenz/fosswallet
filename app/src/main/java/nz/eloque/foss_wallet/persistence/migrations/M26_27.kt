package nz.eloque.foss_wallet.persistence.migrations

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nz.eloque.foss_wallet.utils.map
import org.json.JSONArray
import org.json.JSONException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

private const val TAG = "M26_27"

private val FIELD_COLUMNS = listOf("headerFields", "primaryFields", "secondaryFields", "auxiliaryFields", "backFields")

val M_26_27 =
    object : Migration(26, 27) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val updates = mutableListOf<Pair<String, List<String>>>()

            db.query("SELECT id, ${FIELD_COLUMNS.joinToString(", ")} FROM Pass").use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val columns = FIELD_COLUMNS.indices.map { if (cursor.isNull(it + 1)) "[]" else cursor.getString(it + 1) }
                    val migrated = columns.map { V27FieldContent.migrateColumn(it) }
                    if (migrated != columns) {
                        updates.add(id to migrated)
                    }
                }
            }

            val setClause = FIELD_COLUMNS.joinToString(", ") { "$it = ?" }
            updates.forEach { (id, columns) ->
                db.execSQL("UPDATE Pass SET $setClause WHERE id = ?", (columns + id).toTypedArray())
            }
            Log.i(TAG, "Normalised date fields on ${updates.size} pass(es)")
        }
    }

internal object V27FieldContent {
    private const val PLAIN = 0
    private const val CURRENCY = 1
    private const val DATE = 2
    private const val TIME = 3
    private const val DATE_TIME = 4

    fun migrateColumn(column: String): String =
        try {
            JSONArray(column)
                .map { field ->
                    if (field.has("value")) {
                        field.put("value", migrateContent(field.getString("value")))
                    } else {
                        field
                    }
                }.let { fields -> JSONArray().apply { fields.forEach { put(it) } }.toString() }
        } catch (e: JSONException) {
            Log.w(TAG, "Leaving unparseable field column untouched: $e")
            column
        }

    fun migrateContent(serialized: String): String {
        if (serialized.length < 2 || !serialized[0].isDigit() || serialized[1] != '|') return serialized
        val id = serialized[0].digitToInt()
        if (id != DATE && id != TIME && id != DATE_TIME) return serialized

        val components = serialized.substring(2).split("|")
        val (ignoresTimeZone, isRelative) =
            when (components.size) {
                // Pre-#319 rows, written before timezone handling existed at all.
                2 -> false to false

                // v26 rows. There has never been a 3-component form, so a length of 3 unambiguously
                // means the row is already in v27 form and must be left alone.
                4 -> components[2].toBoolean() to components[3].toBoolean()

                else -> return serialized
            }

        val value = parse(components[0]) ?: return serialized
        val normalized = if (ignoresTimeZone) value.dropOffset() else value
        return "$id|$normalized|${components[1]}|$isRelative"
    }

    /** Discards the offset while keeping the wall-clock reading, which is what the flag asked for. */
    private fun String.dropOffset(): String =
        try {
            ZonedDateTime.parse(this).toLocalDateTime().toString()
        } catch (_: DateTimeParseException) {
            this
        }

    /** Accepts every shape v26 could have written, plus the offsetless ones v27 introduces. */
    private fun parse(value: String): String? =
        listOf<() -> String>(
            { ZonedDateTime.parse(value).toString() },
            { LocalDateTime.parse(value).toString() },
            { LocalDate.parse(value).atStartOfDay().toString() },
        ).firstNotNullOfOrNull { attempt ->
            try {
                attempt()
            } catch (_: DateTimeParseException) {
                null
            }
        }
}

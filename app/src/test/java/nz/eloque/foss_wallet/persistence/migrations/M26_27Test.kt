package nz.eloque.foss_wallet.persistence.migrations

import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassDateTime
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime

class M26_27Test {
    @Test
    fun `ignoresTimeZone rows become bare local date-times`() {
        assertEquals(
            "${PassContent.DATE}|2013-04-24T10:00|MEDIUM|false",
            V27FieldContent.migrateContent("${PassContent.DATE}|2013-04-24T10:00-05:00|MEDIUM|true|false"),
        )
    }

    @Test
    fun `absolute rows keep their offset and only lose the flag`() {
        assertEquals(
            "${PassContent.DATE_TIME}|2013-04-24T10:00-05:00|SHORT|false",
            V27FieldContent.migrateContent("${PassContent.DATE_TIME}|2013-04-24T10:00-05:00|SHORT|false|false"),
        )
    }

    @Test
    fun `isRelative survives the move to slot two`() {
        assertEquals(
            "${PassContent.TIME}|2013-04-24T10:00Z|FULL|true",
            V27FieldContent.migrateContent("${PassContent.TIME}|2013-04-24T10:00Z|FULL|false|true"),
        )
    }

    @Test
    fun `pre-timezone rows with only two slots are upgraded`() {
        // Written before #319 added timezone handling; both flags default to false.
        assertEquals(
            "${PassContent.DATE}|2013-04-24T10:00-05:00|LONG|false",
            V27FieldContent.migrateContent("${PassContent.DATE}|2013-04-24T10:00-05:00|LONG"),
        )
    }

    @Test
    fun `non-date and already-migrated content is left alone`() {
        val untouched =
            listOf(
                "${PassContent.PLAIN}|Gate B12",
                "${PassContent.PLAIN}|weird|text|with|pipes",
                "${PassContent.CURRENCY}|42.00|EUR",
                // Already v27 - three slots. No historical form had three, so this is unambiguous.
                "${PassContent.DATE}|2013-04-24T10:00|MEDIUM|false",
                // Unreadable value: preserved rather than guessed at.
                "${PassContent.DATE}|not-a-date|MEDIUM|true|false",
                "",
                "no leading id",
            )
        untouched.forEach { assertEquals(it, V27FieldContent.migrateContent(it)) }
    }

    @Test
    fun `a whole column is rewritten field by field`() {
        val before =
            """
            [{"key":"depart","label":"DEPART","value":"${PassContent.TIME}|2013-04-24T10:00-05:00|SHORT|true|false"},
             {"key":"gate","label":"GATE","value":"${PassContent.PLAIN}|B12","changeMessage":"Gate changed to %@"}]
            """.trimIndent()
        val after = JSONArray(V27FieldContent.migrateColumn(before))

        assertEquals("${PassContent.TIME}|2013-04-24T10:00|SHORT|false", after.getJSONObject(0).getString("value"))
        assertEquals("${PassContent.PLAIN}|B12", after.getJSONObject(1).getString("value"))
        // Everything other than the value is carried across untouched.
        assertEquals("DEPART", after.getJSONObject(0).getString("label"))
        assertEquals("Gate changed to %@", after.getJSONObject(1).getString("changeMessage"))
    }

    @Test
    fun `malformed column json is preserved rather than dropped`() {
        assertEquals("{not json", V27FieldContent.migrateColumn("{not json"))
    }

    @Test
    fun `migrated rows deserialize into the value the old code rendered`() {
        // The v26 row below was displayed as the wall clock 10:00, because ignoresTimeZone was set.
        val migrated = V27FieldContent.migrateContent("${PassContent.DATE}|2013-04-24T10:00-05:00|MEDIUM|true|false")
        val content = PassContent.deserialize(migrated) as PassContent.Date
        assertEquals(PassDateTime.Floating(LocalDateTime.of(2013, 4, 24, 10, 0)), content.date)

        // And an unflagged row still denotes the same instant it always did.
        val absolute = V27FieldContent.migrateContent("${PassContent.DATE}|2013-04-24T10:00-05:00|MEDIUM|false|false")
        val instant = (PassContent.deserialize(absolute) as PassContent.Date).date
        assertEquals(PassDateTime.Absolute(ZonedDateTime.parse("2013-04-24T10:00-05:00")), instant)
    }
}

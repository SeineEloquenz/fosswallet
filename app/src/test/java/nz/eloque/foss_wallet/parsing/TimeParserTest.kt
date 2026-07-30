package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassDateTime
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle

class TimeParserTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    @Test
    fun `parses the W3C shapes the standard allows`() {
        // Complete date plus hours and minutes - the canonical form in Apple's docs.
        assertTrue(TimeParser.parse("1980-05-07T10:30-05:00") is PassDateTime.Absolute)
        // Plus seconds.
        assertTrue(TimeParser.parse("2013-04-24T10:00:00-05:00") is PassDateTime.Absolute)
        // Plus fractional seconds, Zulu.
        assertTrue(TimeParser.parse("2013-04-24T10:00:00.123Z") is PassDateTime.Absolute)
    }

    @Test
    fun `values without an offset are floating, not a parse failure`() {
        // ZonedDateTime.parse throws on both of these. They used to abort the whole pass import.
        assertEquals(
            PassDateTime.Floating(LocalDateTime.of(2017, 10, 17, 0, 0)),
            TimeParser.parse("2017-10-17T00:00:00"),
        )
        assertEquals(
            PassDateTime.Floating(LocalDateTime.of(2024, 11, 30, 0, 0)),
            TimeParser.parse("2024-11-30"),
        )
    }

    @Test
    fun `garbage yields null rather than throwing`() {
        assertNull(TimeParser.parseOrNull("tomorrow"))
        assertNull(TimeParser.parseOrNull(""))
    }

    @Test
    fun `absolute values render in the reader's zone`() {
        val departure = TimeParser.parse("2013-04-24T10:00-05:00")

        assertEquals(
            ZonedDateTime.of(2013, 4, 24, 17, 0, 0, 0, berlin),
            departure.zonedAt(berlin),
        )
    }

    @Test
    fun `ignoresTimeZone keeps the wall clock in every zone`() {
        val field =
            JSONObject(
                """{"key":"depart","value":"2013-04-24T10:00-05:00","timeStyle":"PKDateStyleShort","ignoresTimeZone":true}""",
            )
        val content = FieldParser.parse(field).content as PassContent.Time

        assertEquals(
            ZonedDateTime.of(2013, 4, 24, 10, 0, 0, 0, berlin),
            content.time.zonedAt(berlin),
        )

        assertEquals(
            ZonedDateTime.of(
                2013,
                4,
                24,
                10,
                0,
                0,
                0,
                ZoneId.of("Pacific/Auckland"),
            ),
            content.time.zonedAt(ZoneId.of("Pacific/Auckland")),
        )
    }

    @Test
    fun `time-only fields convert to the reader's zone by default`() {
        val field =
            JSONObject(
                """{"key":"depart","value":"2013-04-24T10:00-05:00","timeStyle":"PKDateStyleShort"}""",
            )
        val content = FieldParser.parse(field).content as PassContent.Time

        assertEquals(
            ZonedDateTime.of(2013, 4, 24, 17, 0, 0, 0, berlin),
            content.time.zonedAt(berlin),
        )
    }

    @Test
    fun `every style renders without crashing on a time-bearing field`() {
        val absolute = TimeParser.parse("2013-04-24T10:00-05:00")
        val floating = PassDateTime.Floating(LocalDateTime.of(2013, 4, 24, 10, 0))

        for (value in listOf(absolute, floating)) {
            for (style in FormatStyle.entries) {
                assertTrue(PassContent.Date(value, style, false).prettyPrint().isNotBlank())
                assertTrue(PassContent.Time(value, style, false).prettyPrint().isNotBlank())
                assertTrue(PassContent.DateTime(value, style, false).prettyPrint().isNotBlank())
            }
        }
    }

    @Test
    fun `a malformed value degrades to a plain field instead of failing the pass`() {
        val field = JSONObject("""{"key":"depart","value":"see itinerary","dateStyle":"PKDateStyleShort"}""")
        assertTrue(FieldParser.parse(field).content is PassContent.Plain)
    }

    @Test
    fun `content round-trips through the persisted form`() {
        val cases =
            listOf(
                PassContent.Date(TimeParser.parse("2013-04-24T10:00-05:00"), FormatStyle.MEDIUM, false),
                PassContent.Time(TimeParser.parse("2013-04-24T10:00:00.5Z"), FormatStyle.SHORT, false),
                PassContent.DateTime(PassDateTime.Floating(LocalDateTime.of(2024, 1, 2, 3, 4)), FormatStyle.FULL, false),
            )
        cases.forEach { assertEquals(it, PassContent.deserialize(it.serialize())) }
    }

    @Test
    fun `the persisted form no longer carries a redundant timezone flag`() {
        val floating = PassContent.Date(PassDateTime.Floating(LocalDateTime.of(2013, 4, 24, 10, 0)), FormatStyle.MEDIUM, false)
        assertEquals("${PassContent.DATE}|2013-04-24T10:00|MEDIUM|false", floating.serialize())

        val absolute = PassContent.Date(TimeParser.parse("2013-04-24T10:00-05:00"), FormatStyle.MEDIUM, false)
        assertEquals("${PassContent.DATE}|2013-04-24T10:00-05:00|MEDIUM|false", absolute.serialize())
    }
}

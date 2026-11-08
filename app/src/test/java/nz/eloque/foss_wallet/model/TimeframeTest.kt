package nz.eloque.foss_wallet.model

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZonedDateTime

class TimeframeTest {
    private fun semantics(json: String) = Semantics.fromJson(JSONObject(json))

    private fun pass(
        semantics: String? = null,
        relevantDates: List<PassRelevantDate> = listOf(),
    ) = Pass(
        id = "pass-1",
        description = "Ticket",
        formatVersion = 1,
        organization = "org",
        serialNumber = "serial",
        type = PassType.Event,
        barCodes = setOf(),
        addedAt = Instant.ofEpochMilli(0),
        relevantDates = relevantDates,
        semantics = semantics?.let { semantics(it) },
    )

    private fun date(value: String) = ZonedDateTime.parse(value)

    @Test
    fun `uses the semantic event`() {
        val timeframe =
            pass(
                semantics =
                    """{"eventStartDate":"2026-12-01T19:00:00+01:00","eventEndDate":"2026-12-01T22:00:00+01:00","venueName":"Stadium"}""",
                relevantDates = listOf(PassRelevantDate.Date(date("2026-11-30T10:00:00+01:00"))),
            ).timeframe()

        assertEquals(Timeframe(date("2026-12-01T19:00:00+01:00"), date("2026-12-01T22:00:00+01:00"), "Stadium"), timeframe)
    }

    @Test
    fun `derives the event end from its duration`() {
        val timeframe = semantics("""{"eventStartDate":"2026-12-01T19:00:00+01:00","duration":5400}""").timeframe()

        assertEquals(date("2026-12-01T20:30:00+01:00"), timeframe?.end)
    }

    @Test
    fun `uses the current departure and arrival of a journey`() {
        val timeframe =
            semantics(
                """{"originalDepartureDate":"2026-12-01T08:00:00Z","currentDepartureDate":"2026-12-01T08:40:00Z",
                |"originalArrivalDate":"2026-12-01T11:00:00Z","currentArrivalDate":"2026-12-01T11:30:00Z",
                |"departureStationName":"Zürich HB","departureLocationDescription":"Zurich"}
                """.trimMargin(),
            ).timeframe()

        assertEquals(Timeframe(date("2026-12-01T08:40:00Z"), date("2026-12-01T11:30:00Z"), "Zürich HB"), timeframe)
    }

    @Test
    fun `prefers the delayed departure plus duration over the original arrival`() {
        val timeframe =
            semantics(
                """{"currentDepartureDate":"2026-12-01T08:40:00Z","duration":3600,"originalArrivalDate":"2026-12-01T09:00:00Z"}""",
            ).timeframe()

        assertEquals(date("2026-12-01T09:40:00Z"), timeframe?.end)
    }

    @Test
    fun `falls back to relevant dates`() {
        val interval = PassRelevantDate.DateInterval(date("2026-12-01T08:00:00Z"), date("2026-12-01T09:00:00Z"))

        assertEquals(Timeframe(interval.startDate, interval.endDate), pass("""{"venueName":"Stadium"}""", listOf(interval)).timeframe())
    }

    @Test
    fun `has no timeframe without dates`() {
        assertNull(pass("""{"venueName":"Stadium"}""").timeframe())
    }

    @Test
    fun `localizes semantic strings`() {
        val localized =
            pass("""{"eventName":"finalKey","venueName":"Stadium"}""")
                .applyLocalization(mapOf("finalKey" to PassLocalization("pass-1", "de", "finalKey", "Finale")))
                .semantics

        assertEquals("Finale", localized?.eventName)
        assertEquals("Stadium", localized?.venueName)
    }
}

package nz.eloque.foss_wallet.ui.screens.create

import androidx.compose.ui.graphics.Color
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassRelevantDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZonedDateTime

class CreateFormTest {
    private val start = ZonedDateTime.parse("2026-10-04T18:00+02:00[Europe/Berlin]")
    private val end = ZonedDateTime.parse("2026-10-04T20:00+02:00[Europe/Berlin]")

    @Test
    fun `start and end make a date interval`() {
        assertEquals(
            listOf(PassRelevantDate.DateInterval(start, end)),
            CreateForm(relevantStart = start, relevantEnd = end).relevantDates(),
        )
    }

    @Test
    fun `start alone makes a single date`() {
        assertEquals(listOf(PassRelevantDate.Date(start)), CreateForm(relevantStart = start).relevantDates())
    }

    @Test
    fun `no start means no relevant dates`() {
        assertEquals(emptyList<PassRelevantDate>(), CreateForm(relevantEnd = end).relevantDates())
    }

    @Test
    fun `no colors means default pass colors`() {
        assertNull(CreateForm().passColors())
    }

    @Test
    fun `missing colors fall back to the first chosen color`() {
        assertEquals(
            PassColors(background = Color.Red, foreground = Color.Blue, label = Color.Red),
            CreateForm(backgroundColor = Color.Red, foregroundColor = Color.Blue).passColors(),
        )
        assertEquals(
            PassColors(background = Color.Green, foreground = Color.Green, label = Color.Green),
            CreateForm(labelColor = Color.Green).passColors(),
        )
    }
}

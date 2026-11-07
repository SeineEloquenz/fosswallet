package nz.eloque.foss_wallet.model.field

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class CurrencyFormatTest {
    @Test
    fun `formats in the conventions of the locale`() {
        assertEquals("$12.50", PassContent.Currency("12.5", "USD").format(Locale.US))
        assertEquals("€1,234.00", PassContent.Currency("1234", "EUR").format(Locale.US))
        assertEquals("1.234,00 €", PassContent.Currency("1234", "EUR").format(Locale.GERMANY).replace(' ', ' '))
    }

    @Test
    fun `uses the fraction digits of the currency`() {
        assertEquals("¥500", PassContent.Currency("500", "JPY").format(Locale.JAPAN).replace('￥', '¥'))
    }

    @Test
    fun `keeps non numeric amounts with the currency code`() {
        assertEquals("free USD", PassContent.Currency("free", "USD").format(Locale.US))
    }

    @Test
    fun `appends unknown currency codes`() {
        assertEquals("12.5 PTS", PassContent.Currency("12.5", "PTS").format(Locale.US))
    }
}

package nz.eloque.foss_wallet.model.field

import android.icu.util.ULocale
import nz.eloque.foss_wallet.parsing.FieldParser
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NumberStyleTest {
    @Test
    fun `formats every number style`() {
        assertEquals("1,234.5", PassContent.Numeric("1234.5", NumberStyle.DECIMAL).format(ULocale.US))
        assertEquals("25%", PassContent.Numeric("0.25", NumberStyle.PERCENT).format(ULocale.US))
        assertEquals("1.2345E3", PassContent.Numeric("1234.5", NumberStyle.SCIENTIFIC).format(ULocale.US))
        assertEquals("forty-two", PassContent.Numeric("42", NumberStyle.SPELL_OUT).format(ULocale.US))
    }

    @Test
    fun `formats in the conventions of the locale`() {
        assertEquals("1.234,5", PassContent.Numeric("1234.5", NumberStyle.DECIMAL).format(ULocale.GERMANY))
    }

    @Test
    fun `parses numeric values with a number style`() {
        val field = JSONObject().put("key", "k").put("value", 0.25).put("numberStyle", "PKNumberStylePercent")

        assertEquals(PassContent.Numeric("0.25", NumberStyle.PERCENT), FieldParser.parse(field).content)
    }

    @Test
    fun `keeps non numeric values and unknown styles as plain text`() {
        val text = JSONObject().put("key", "k").put("value", "Gold").put("numberStyle", "PKNumberStyleDecimal")
        val unknown = JSONObject().put("key", "k").put("value", 5).put("numberStyle", "PKNumberStyleUnknown")

        assertEquals(PassContent.Plain("Gold"), FieldParser.parse(text).content)
        assertEquals(PassContent.Plain("5"), FieldParser.parse(unknown).content)
    }

    @Test
    fun `survives serialization`() {
        val content = PassContent.Numeric("42", NumberStyle.SPELL_OUT)

        assertEquals(content, PassContent.deserialize(content.serialize()))
    }
}

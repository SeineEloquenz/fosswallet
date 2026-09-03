package nz.eloque.foss_wallet.parsing

import junit.framework.TestCase.assertEquals
import nz.eloque.foss_wallet.model.PassLocalization
import org.junit.Test

class LocalizationParserTest {
    @Test
    fun `parses a simple strings file`() {
        val content =
            """
            "EVENT_LABEL" = "Event";
            "EVENT_VALUE" = "Demo Concert 2026";
            """.trimIndent()

        val result = LocalizationParser.parseStrings("en", content)

        assertEquals(
            setOf(
                PassLocalization("", "en", "EVENT_LABEL", "Event"),
                PassLocalization("", "en", "EVENT_VALUE", "Demo Concert 2026"),
            ),
            result,
        )
    }

    // Regression test for #631
    @Test
    fun `a URL value containing double slashes is not mistaken for a line comment`() {
        val content =
            """
            "PRIVACY_LABEL" = "Privacy Policy";
            "PRIVACY_VALUE" = "https://privacy.ticketmaster.de/en/privacy-policy";

            "TERMS_AND_CONDITIONS_LABEL" = "Terms & Conditions";
            "TERMS_AND_CONDITIONS_LINK_LABEL" = "Terms of use";
            "TERMS_AND_CONDITIONS_LINK_VALUE" = "https://www.ticketmaster.de/help/terms.html?language=en-us";
            """.trimIndent()

        val result = LocalizationParser.parseStrings("en", content)
        val byKey = result.associateBy { it.label }

        assertEquals(5, result.size)
        assertEquals("https://privacy.ticketmaster.de/en/privacy-policy", byKey["PRIVACY_VALUE"]?.text)
        assertEquals("Terms & Conditions", byKey["TERMS_AND_CONDITIONS_LABEL"]?.text)
        assertEquals("Terms of use", byKey["TERMS_AND_CONDITIONS_LINK_LABEL"]?.text)
        assertEquals(
            "https://www.ticketmaster.de/help/terms.html?language=en-us",
            byKey["TERMS_AND_CONDITIONS_LINK_VALUE"]?.text,
        )
    }

    @Test
    fun `block comments outside strings are stripped`() {
        val content =
            """
            /* This describes the key below */
            "KEY" = "value";
            """.trimIndent()

        val result = LocalizationParser.parseStrings("en", content)

        assertEquals(setOf(PassLocalization("", "en", "KEY", "value")), result)
    }

    @Test
    fun `trailing line comments outside strings are stripped`() {
        val content = "\"KEY\" = \"value\"; // note about this key\n"

        val result = LocalizationParser.parseStrings("en", content)

        assertEquals(setOf(PassLocalization("", "en", "KEY", "value")), result)
    }

    @Test
    fun `a value containing an escaped quote is not treated as ending the string`() {
        val content = "\"KEY\" = \"He said \\\"hi//there\\\"\";\n"

        val result = LocalizationParser.parseStrings("en", content)

        assertEquals(setOf(PassLocalization("", "en", "KEY", "He said \"hi//there\"")), result)
    }

    @Test
    fun `a value that itself contains a block-comment-like sequence is preserved`() {
        val content = "\"KEY\" = \"path is a/*b*/c\";\n"

        val result = LocalizationParser.parseStrings("en", content)

        assertEquals(setOf(PassLocalization("", "en", "KEY", "path is a/*b*/c")), result)
    }
}

package nz.eloque.foss_wallet.utils

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import nz.eloque.foss_wallet.model.PassLocalization
import org.junit.Test

class LocaleMappingTest {
    @Test
    fun `returns exact locale match`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "en", "title", "English Title"),
                PassLocalization("pass-1", "de", "title", "Deutscher Titel"),
                PassLocalization("pass-1", "de", "subtitle", "Untertitel"),
            )

        val result = localizations.toMapping("de")

        assertEquals(2, result.size)

        assertEquals("Deutscher Titel", result["title"]?.text)
        assertEquals("Untertitel", result["subtitle"]?.text)

        assertTrue(result.values.all { it.lang() == "de" })
    }

    @Test
    fun `falls back to english when requested locale is unavailable`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "en", "title", "English Title"),
                PassLocalization("pass-1", "en", "subtitle", "English Subtitle"),
                PassLocalization("pass-1", "fr", "title", "Titre Français"),
            )

        val result = localizations.toMapping("de")

        assertEquals(2, result.size)

        assertEquals("English Title", result["title"]?.text)
        assertEquals("English Subtitle", result["subtitle"]?.text)

        assertTrue(result.values.all { it.lang() == "en" })
    }

    @Test
    fun `matches locale variants using RFC 4647 lookup`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "en", "title", "English Title"),
                PassLocalization("pass-1", "en", "subtitle", "English Subtitle"),
            )

        val result = localizations.toMapping("en-US")

        assertEquals(2, result.size)
        assertTrue(result.values.all { it.lang() == "en" })
    }

    @Test
    fun `prefers requested locale over english fallback`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "en", "title", "English Title"),
                PassLocalization("pass-1", "fr", "title", "Titre Français"),
            )

        val result = localizations.toMapping("fr")

        assertEquals(1, result.size)

        assertEquals("Titre Français", result["title"]?.text)
        assertEquals("fr", result["title"]?.lang())
    }

    @Test
    fun `normalizes language tags before matching`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "EN_us", "title", "English US"),
                PassLocalization("pass-1", "de_de", "title", "Deutsch"),
            )

        val result = localizations.toMapping("en-US")

        assertEquals(1, result.size)
        assertEquals("English US", result["title"]?.text)
    }

    @Test
    fun `returns empty map for empty input`() {
        val result = emptyList<PassLocalization>().toMapping("en")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty map when no locale matches and english is unavailable`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "fr", "title", "Titre"),
                PassLocalization("pass-1", "es", "subtitle", "Subtitulo"),
            )

        val result = localizations.toMapping("de")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `duplicate labels keep the last value`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "en", "title", "First"),
                PassLocalization("pass-1", "en", "title", "Second"),
            )

        val result = localizations.toMapping("en")

        assertEquals(1, result.size)
        assertEquals("Second", result["title"]?.text)
    }

    @Test
    fun `only entries from best matched locale are included`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "en", "title", "English"),
                PassLocalization("pass-1", "en", "subtitle", "English Subtitle"),
                PassLocalization("pass-1", "de", "title", "Deutsch"),
                PassLocalization("pass-1", "fr", "footer", "Français"),
            )

        val result = localizations.toMapping("de")

        assertEquals(setOf("title"), result.keys)
        assertEquals("Deutsch", result["title"]?.text)
    }

    @Test
    fun `supports script based locale matching`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "zh-Hant", "title", "Traditional Chinese"),
                PassLocalization("pass-1", "en", "title", "English"),
            )

        val result = localizations.toMapping("zh-Hant-TW")

        assertEquals(1, result.size)
        assertEquals("Traditional Chinese", result["title"]?.text)
    }

    @Test
    fun `supports region specific locale matching`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "pt-BR", "title", "Português Brasil"),
                PassLocalization("pass-1", "pt-PT", "title", "Português Portugal"),
            )

        val result = localizations.toMapping("pt-BR")

        assertEquals(1, result.size)
        assertEquals("Português Brasil", result["title"]?.text)
    }

    @Test
    fun `falls back from region specific locale to base language`() {
        val localizations =
            listOf(
                PassLocalization("pass-1", "pt", "title", "Português"),
            )

        val result = localizations.toMapping("pt-BR")

        assertEquals(1, result.size)
        assertEquals("Português", result["title"]?.text)
    }
}

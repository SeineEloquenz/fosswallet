package nz.eloque.foss_wallet.persistence.loader

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.Test

class TextDecoderTest {
    private val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    private val utf16leBom = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    private val utf16beBom = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

    @Test
    fun `decodes plain utf8 without a BOM`() {
        val decoded = TextDecoder.decode("\"KEY\" = \"value\";".toByteArray(Charsets.UTF_8))

        assertEquals("\"KEY\" = \"value\";", decoded)
    }

    // Regression: a UTF-8 BOM must not survive as a leading U+FEFF, which corrupted the first key.
    @Test
    fun `strips a utf8 BOM instead of leaking it as U+FEFF`() {
        val bytes = utf8Bom + "\"KEY\" = \"value\";".toByteArray(Charsets.UTF_8)

        val decoded = TextDecoder.decode(bytes)

        assertEquals("\"KEY\" = \"value\";", decoded)
        assertFalse("decoded content must not start with U+FEFF", decoded.startsWith('﻿'))
    }

    @Test
    fun `strips a utf16-le BOM and decodes as utf16-le`() {
        val bytes = utf16leBom + "héllo".toByteArray(Charsets.UTF_16LE)

        val decoded = TextDecoder.decode(bytes)

        assertEquals("héllo", decoded)
    }

    @Test
    fun `strips a utf16-be BOM and decodes as utf16-be`() {
        val bytes = utf16beBom + "héllo".toByteArray(Charsets.UTF_16BE)

        val decoded = TextDecoder.decode(bytes)

        assertEquals("héllo", decoded)
    }

    @Test
    fun `handles empty input`() {
        assertEquals("", TextDecoder.decode(ByteArray(0)))
    }
}

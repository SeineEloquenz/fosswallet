package nz.eloque.foss_wallet.persistence.loader

import java.nio.charset.Charset

/**
 * Decodes text entries (pass.json, pass.strings) from a pkpass archive.
 */
object TextDecoder {
    private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    private val UTF16LE_BOM = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    private val UTF16BE_BOM = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

    fun decode(bytes: ByteArray): String = stripBom(bytes).toString(detectEncoding(bytes))

    fun detectEncoding(bytes: ByteArray): Charset =
        when {
            bytes.startsWith(UTF8_BOM) -> Charsets.UTF_8
            bytes.startsWith(UTF16LE_BOM) -> Charsets.UTF_16LE
            bytes.startsWith(UTF16BE_BOM) -> Charsets.UTF_16BE
            else -> Charsets.UTF_8 // fallback (could be wrong, but UTF-8 is common)
        }

    private fun stripBom(bytes: ByteArray): ByteArray =
        when {
            bytes.startsWith(UTF8_BOM) -> bytes.copyOfRange(UTF8_BOM.size, bytes.size)
            bytes.startsWith(UTF16LE_BOM) -> bytes.copyOfRange(UTF16LE_BOM.size, bytes.size)
            bytes.startsWith(UTF16BE_BOM) -> bytes.copyOfRange(UTF16BE_BOM.size, bytes.size)
            else -> bytes
        }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (this.size < prefix.size) return false
        return prefix.indices.all { this[it] == prefix[it] }
    }
}

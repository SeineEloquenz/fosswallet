package nz.eloque.foss_wallet.persistence.loader

import nz.eloque.foss_wallet.parsing.PassParser
import org.junit.Assert.assertThrows
import org.junit.Test

class PassLoaderTest {
    private val loader = PassLoader(PassParser())

    @Test
    fun `bytes that are not a pkpass are rejected`() {
        assertThrows(InvalidPassException::class.java) { loader.load("not a pkpass".toByteArray()) }
    }

    @Test
    fun `empty bytes are rejected`() {
        assertThrows(InvalidPassException::class.java) { loader.load(ByteArray(0)) }
    }
}

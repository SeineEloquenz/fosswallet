package nz.eloque.foss_wallet.model

import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class UnsupportedBarcodeFormatTest(
    private val format: String,
) {
    @Test
    fun `formats without an encoder require a fallback`() {
        assertNull(BarCode.formatFromString(format))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<String>> =
            listOf("MAXICODE", "RSS_14", "RSS_EXPANDED", "UPC_EAN_EXTENSION", "MICRO_QR_CODE", "MAXI_CODE", "DATA_BAR", "RMQR_CODE")
                .map { arrayOf(it) }
    }
}

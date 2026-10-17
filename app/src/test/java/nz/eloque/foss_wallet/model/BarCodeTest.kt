package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import org.junit.Assert
import org.junit.Test

class BarCodeTest {
    @Test
    fun mapsDataMatrixFormat() {
        Assert.assertEquals(BarcodeFormat.DATA_MATRIX, BarCode.formatFromString("PKBarcodeFormatDataMatrix"))
    }

    @Test
    fun unknownFormatFallsBackToQr() {
        Assert.assertEquals(BarcodeFormat.QR_CODE, BarCode.formatFromString("PKBarcodeFormatUnknown"))
    }

    @Test
    fun encodesDataMatrix() {
        val barcode = BarCode(BarcodeFormat.DATA_MATRIX, "008602576300111020101000", Charsets.ISO_8859_1, null)
        Assert.assertNotNull(barcode.encode())
    }
}

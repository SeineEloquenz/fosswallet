package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IataBcbpTest {

    private val validBcbp = "M1" +
            "DOE/JOHN            " +
            "E" +
            "ABC1234" +
            "JFK" +
            "LAX" +
            "AA " +
            "00123" +
            "123" +
            "Y" +
            "012A" +
            "00001" +
            "0" +
            "AB"

    @Test
    fun recognizesBcbpInSupportedFormats() {
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.AZTEC, validBcbp))
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.PDF_417, validBcbp))
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.QR_CODE, validBcbp))
    }

    @Test
    fun recognizesBcbpWithSymbologyPrefix() {
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.QR_CODE, "]Q3$validBcbp"))
    }

    @Test
    fun rejectsUnsupportedFormats() {
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.CODE_128, validBcbp))
    }

    @Test
    fun rejectsClearlyInvalidPayloads() {
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.AZTEC, "M1short"))
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.AZTEC, "M1Doe/John            E" + "X".repeat(40)))
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.AZTEC, "Q1DOE/JOHN            E" + "X".repeat(40)))
    }

    @Test
    fun extractsRelevantData() {
        val parsed = IataBcbp.parse(BarcodeFormat.AZTEC, validBcbp)
        assertNotNull(parsed)
        assertEquals("John Doe", parsed!!.passengerName)
        assertEquals("JFK", parsed.fromAirport)
        assertEquals("LAX", parsed.toAirport)
        assertEquals("AA123", parsed.flightCode())
        assertEquals("12A", parsed.seat)
        assertEquals("ABC1234", parsed.pnr)
        assertEquals("1", parsed.checkInSequence)
        assertTrue(parsed.summary().contains("JFK->LAX"))
    }
}

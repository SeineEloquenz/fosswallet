package nz.eloque.foss_wallet.model

import com.google.zxing.BarcodeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IataBcbpTest {

    private val basicBcbp = "M1DESMARAIS/LUC       EABC123 YULFRAAC 0834 226F001A0025 106>60000"
    private val multiLegWithSecurityBcbp =
        "M2DESMARAIS/LUC       EABC123 YULFRAAC 0834 226F001A0025 14D>6181WW6225BAC 00141234560032A0141234567890 1AC AC 1234567890123    20KYLX58ZDEF456 FRAGVALH 3664 227C012C0002 12E2A0140987654321 1AC AC 1234567890123    2PCNWQ^164GIWVC5EH7JNT684FVNJ91W2QA4DVN5J8K4F0L0GEQ3DF5TGBN8709HKT5D3DW3GBHFCVHMY7J5T6HFR41W2QA4DVN5J8K4F0L0GE"
    private val lufthansaAztecBcbpWithTrailingSpaces =
        "M1MUNDLER/NIELS       EX4TE6N ZRHHAMLX 1056 049Y030F0117 377>8320 W    BLX                                        2A72463496679170 LX LH 992221992624215     Y*30600000K09  LHS    "

    @Test
    fun recognizesBcbpInSupportedFormats() {
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.AZTEC, basicBcbp))
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.PDF_417, basicBcbp))
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.QR_CODE, basicBcbp))
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.DATA_MATRIX, basicBcbp))
    }

    @Test
    fun recognizesBcbpWithSymbologyPrefix() {
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.QR_CODE, "]Q3$basicBcbp"))
    }

    @Test
    fun rejectsUnsupportedFormats() {
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.CODE_128, basicBcbp))
    }

    @Test
    fun rejectsClearlyInvalidPayloads() {
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.AZTEC, "M1short"))
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.AZTEC, "M1Doe/John            E" + "X".repeat(40)))
        assertFalse(IataBcbp.isBcbp(BarcodeFormat.AZTEC, "Q1DOE/JOHN            E" + "X".repeat(40)))
    }

    @Test
    fun extractsRelevantData() {
        val parsed = IataBcbp.parse(BarcodeFormat.AZTEC, basicBcbp)
        assertNotNull(parsed)
        assertEquals("Luc Desmarais", parsed!!.passengerName)
        assertEquals("YUL", parsed.fromAirport)
        assertEquals("FRA", parsed.toAirport)
        assertEquals("AC834", parsed.flightCode())
        assertEquals("1A", parsed.seat)
        assertEquals("ABC123", parsed.pnr)
        assertEquals("25", parsed.checkInSequence)
        assertEquals(1, parsed.numberOfLegs)
        assertEquals(">", parsed.versionNumberIndicator)
        assertEquals(6, parsed.versionNumber)
    }

    @Test
    fun parsesMultiLegAndSecurityData() {
        val parsed = IataBcbp.parse(BarcodeFormat.PDF_417, multiLegWithSecurityBcbp)
        assertNotNull(parsed)
        assertEquals(2, parsed!!.numberOfLegs)
        assertEquals("Luc Desmarais", parsed.passengerName)
        assertEquals("YUL", parsed.legs[0].fromAirport)
        assertEquals("FRA", parsed.legs[0].toAirport)
        assertEquals("FRA", parsed.legs[1].fromAirport)
        assertEquals("GVA", parsed.legs[1].toAirport)
        assertEquals("LH3664", parsed.legs[1].flightCode())
        assertEquals("12C", parsed.legs[1].seatNumber)
        assertEquals("1", parsed.securityData?.type)
        assertTrue((parsed.securityData?.data?.length ?: 0) > 40)
        assertEquals("0014123456003", parsed.uniqueConditional?.bagTagNumbers?.firstOrNull())
    }

    @Test
    fun recognizesBcbpWithMeaningfulTrailingSpaces() {
        val parsed = IataBcbp.parse(BarcodeFormat.AZTEC, lufthansaAztecBcbpWithTrailingSpaces)
        assertNotNull(parsed)
        assertTrue(IataBcbp.isBcbp(BarcodeFormat.AZTEC, lufthansaAztecBcbpWithTrailingSpaces))
        assertEquals("Niels Mundler", parsed!!.passengerName)
        assertEquals("ZRH", parsed.fromAirport)
        assertEquals("HAM", parsed.toAirport)
        assertEquals("LX1056", parsed.flightCode())
    }
}

package nz.eloque.foss_wallet.ui

import androidx.core.net.toUri
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.shortcut.ShortcutService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NavigationTest {
    @Test
    fun `create route without a barcode has no barcode`() {
        assertNull(Route.Create().toBarCode())
    }

    @Test
    fun `barcode format and non-default encoding survive navigation`() {
        val barcode = BarCode(BarcodeFormat.AZTEC, "Zürich", Charsets.ISO_8859_1, "Ticket")
        assertEquals(barcode, Route.Create(barcode).toBarCode())
        assertEquals(barcode, Route.ScanResult(barcode).toBarCode())
    }

    @Test
    fun `pinned pass shortcuts open the pass`() {
        assertEquals("pass-id", ShortcutService.passId("${ShortcutService.BASE_URI}/pass-id".toUri()))
    }

    @Test
    fun `other uris are not pass shortcuts`() {
        assertNull(ShortcutService.passId("content://media/external/file/1".toUri()))
        assertNull(ShortcutService.passId(null))
    }
}

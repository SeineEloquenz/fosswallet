package nz.eloque.foss_wallet.ui.screens.create

import android.content.ContentResolver
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreviewDocumentTest {
    private val resolver = mock(ContentResolver::class.java)

    @Test
    fun `shared PDF with opaque URI uses sender MIME type`() {
        assertEquals(ScanSource.Pdf, resolver.scanSource(Uri.parse("content://sender/123"), "application/pdf"))
    }

    @Test
    fun `picked image uses provider MIME type`() {
        val uri = Uri.parse("content://picker/123")
        `when`(resolver.getType(uri)).thenReturn("image/jpeg")
        assertEquals(ScanSource.Image, resolver.scanSource(uri))
    }

    @Test
    fun `generic MIME type falls back to filename extension`() {
        val uri = Uri.parse("content://picker/TICKET.PDF")
        `when`(resolver.getType(uri)).thenReturn("application/octet-stream")
        assertEquals(ScanSource.Pdf, resolver.scanSource(uri))
    }

    @Test
    fun `pkpass stays on the pass import path`() {
        val uri = Uri.parse("content://picker/ticket.pkpass")
        `when`(resolver.getType(uri)).thenReturn("application/vnd.apple.pkpass")
        assertNull(resolver.scanSource(uri))
    }
}

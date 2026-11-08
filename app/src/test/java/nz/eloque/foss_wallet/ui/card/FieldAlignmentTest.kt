package nz.eloque.foss_wallet.ui.card

import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import nz.eloque.foss_wallet.model.field.TextAlignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FieldAlignmentTest {
    @Test
    fun `maps explicit alignments to absolute or centered alignments`() {
        assertEquals(TextAlign.Left, TextAlignment.LEFT.horizontalAlignment()?.textAlign())
        assertEquals(TextAlign.Center, TextAlignment.CENTER.horizontalAlignment()?.textAlign())
        assertEquals(TextAlign.Right, TextAlignment.RIGHT.horizontalAlignment()?.textAlign())
    }

    @Test
    fun `natural alignment keeps the layout default`() {
        assertNull(TextAlignment.NATURAL.horizontalAlignment())
    }

    @Test
    fun `layout alignments keep their text alignment`() {
        assertEquals(TextAlign.Start, Alignment.Start.textAlign())
        assertEquals(TextAlign.End, Alignment.End.textAlign())
        assertEquals(TextAlign.Center, Alignment.CenterHorizontally.textAlign())
        assertEquals(TextAlign.Right, AbsoluteAlignment.Right.textAlign())
    }
}

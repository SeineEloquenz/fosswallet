package nz.eloque.foss_wallet.ui.screens.create

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PreviewViewportTest {
    @Test
    fun `drag can move the file completely outside the viewport`() {
        val moved =
            PreviewViewport(1200, 600, 400, 800)
                .transformed(200f, 400f, 900f, -1100f, 1f)
        assertEquals(900f, moved.panX, 0.001f)
        assertEquals(-1100f, moved.panY, 0.001f)
        assertNull(moved.visibleBounds())
    }

    @Test
    fun `off center pinch preserves the image point beneath the fingers`() {
        val before = PreviewViewport(1200, 600, 400, 800)
        val after = before.transformed(60f, 350f, 0f, 0f, 2f)
        assertEquals((60f - before.left) / before.scale, (60f - after.left) / after.scale, 0.001f)
        assertEquals((350f - before.top) / before.scale, (350f - after.top) / after.scale, 0.001f)
    }

    @Test
    fun `pinch and simultaneous drag keep the anchor beneath the moving fingers`() {
        val before = PreviewViewport(1200, 2400, 800, 400, zoom = 3f, panX = 120f, panY = -80f)
        val after = before.transformed(700f, 50f, -40f, 75f, 0.5f)
        assertEquals((700f - before.left) / before.scale, (660f - after.left) / after.scale, 0.001f)
        assertEquals((50f - before.top) / before.scale, (125f - after.top) / after.scale, 0.001f)
    }

    @Test
    fun `pinching out can shrink the file below its initial fit`() {
        val after = PreviewViewport(1200, 600, 400, 800).transformed(100f, 300f, 0f, 0f, 0.5f)
        assertEquals(0.5f, after.zoom, 0.001f)
    }

    @Test
    fun `initial fit includes whole image with letterboxing`() {
        assertEquals(ImageBounds(0, 0, 1200, 600), PreviewViewport(1200, 600, 400, 800).visibleBounds())
    }

    @Test
    fun `zoom crops to visible image coordinates`() {
        assertEquals(ImageBounds(300, 300, 600, 600), PreviewViewport(1200, 1200, 400, 400, zoom = 2f).visibleBounds())
    }

    @Test
    fun `panning selects the other barcode region`() {
        assertEquals(ImageBounds(600, 300, 600, 600), PreviewViewport(1200, 1200, 400, 400, zoom = 2f, panX = -200f).visibleBounds())
    }

    @Test
    fun `portrait PDF crops correctly in landscape viewport`() {
        assertEquals(ImageBounds(0, 900, 1200, 600), PreviewViewport(1200, 2400, 800, 400, zoom = 4f).visibleBounds())
    }

    @Test
    fun `image outside viewport is never scanned`() {
        assertNull(PreviewViewport(1200, 1200, 400, 400, panX = 1000f).visibleBounds())
    }
}

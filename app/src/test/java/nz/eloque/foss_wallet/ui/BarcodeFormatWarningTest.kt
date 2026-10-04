package nz.eloque.foss_wallet.ui

import android.app.Activity
import android.app.AlertDialog
import android.os.Looper
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.ui.screens.create.FileScanner
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BarcodeFormatWarningTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `waits for OK and keeps all unsupported formats visible`() =
        runTest {
            Robolectric.buildActivity(Activity::class.java).setup().use { activity ->
                val job = launch { showBarcodeFormatWarning(activity.get(), linkedSetOf("Unknown", "Other")) }
                runCurrent()

                val dialog = ShadowAlertDialog.getLatestAlertDialog()
                assertTrue(dialog.isShowing)
                assertEquals(
                    "The following barcode formats are unsupported:\nUnknown\nOther\n\n" +
                        "They will be replaced with QR codes, which may not scan correctly.",
                    shadowOf(dialog).message,
                )
                dialog.onBackPressed()
                assertTrue(dialog.isShowing)
                assertFalse(job.isCompleted)

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
                shadowOf(Looper.getMainLooper()).idle()
                runCurrent()

                assertFalse(dialog.isShowing)
                assertTrue(job.isCompleted)
            }
        }

    @Test
    fun `unsupported scans wait for OK before returning the QR fallback`() =
        runTest {
            Robolectric.buildActivity(Activity::class.java).setup().use { activity ->
                listOf("MICRO_QR_CODE", "MAXI_CODE", "RSS_14").forEach { format ->
                    val text = "A\r\nB & Zürich"
                    val result = async { FileScanner.ScanResult(text, format).toBarCode(activity.get()) }
                    runCurrent()

                    val dialog = ShadowAlertDialog.getLatestAlertDialog()
                    assertTrue(dialog.isShowing)
                    assertTrue(shadowOf(dialog).message.toString().contains(format))
                    assertFalse(result.isCompleted)

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
                    shadowOf(Looper.getMainLooper()).idle()
                    runCurrent()

                    assertEquals(BarCode(BarcodeFormat.QR_CODE, text, Charsets.UTF_8, text), result.await())
                }
            }
        }

    @Test
    fun `supported scans keep their format without a warning`() =
        runTest {
            Robolectric.buildActivity(Activity::class.java).setup().use { activity ->
                val result = FileScanner.ScanResult("ABC123", "CODE_128").toBarCode(activity.get())

                assertEquals(BarCode(BarcodeFormat.CODE_128, "ABC123", Charsets.UTF_8, "ABC123"), result)
                assertNull(ShadowAlertDialog.getLatestAlertDialog())
            }
        }

    @Test
    fun `cancelling the import dismisses its dialog`() =
        runTest {
            Robolectric.buildActivity(Activity::class.java).setup().use { activity ->
                val job = launch { showBarcodeFormatWarning(activity.get(), setOf("Unknown")) }
                runCurrent()
                val dialog = ShadowAlertDialog.getLatestAlertDialog()

                job.cancel()
                shadowOf(Looper.getMainLooper()).idle()
                runCurrent()

                assertFalse(dialog.isShowing)
            }
        }
}

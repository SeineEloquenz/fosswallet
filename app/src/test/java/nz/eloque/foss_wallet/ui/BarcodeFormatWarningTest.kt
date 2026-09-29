package nz.eloque.foss_wallet.ui

import android.app.Activity
import android.app.AlertDialog
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

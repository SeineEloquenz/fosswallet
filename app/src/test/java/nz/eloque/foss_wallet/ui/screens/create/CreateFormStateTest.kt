package nz.eloque.foss_wallet.ui.screens.create

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Parcel
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import com.google.zxing.BarcodeFormat
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CreateFormStateTest {
    @Test
    fun `scanned barcode route starts the form with that barcode`() {
        val handle =
            SavedStateHandle(
                mapOf("format" to "AZTEC", "message" to "#UT01\u0000x", "encoding" to "ISO-8859-1"),
            )

        val form = CreateFormState(handle).form.value

        assertEquals(listOf(BarcodeDraft("#UT01\u0000x", "", BarcodeFormat.AZTEC, Charsets.ISO_8859_1)), form.barcodes)
    }

    @Test
    fun `plain create route starts with an empty form`() {
        assertEquals(CreateForm(), CreateFormState(SavedStateHandle()).form.value)
    }

    @SuppressLint("RestrictedApi")
    @Test
    fun `every edited field survives process death`() {
        val edited =
            CreateForm(
                name = "Ticket",
                nameTouched = true,
                organization = "Org",
                serialNumber = "42",
                barcodes = listOf(BarcodeDraft("x\u0000y", "", BarcodeFormat.AZTEC, Charsets.ISO_8859_1)),
                type = PassType.Boarding(TransitType.TRAIN),
                location =
                    Location("").apply {
                        latitude = 49.0
                        longitude = 8.4
                    },
                relevantStart = ZonedDateTime.parse("2026-10-04T18:00+02:00[Europe/Berlin]"),
                expirationDate = ZonedDateTime.parse("2026-10-05T00:00Z"),
                backgroundColor = Color(0xFF1E88E5),
                iconUrl = "content://media/external/images/1".toUri(),
            )
        val handle = SavedStateHandle()
        CreateFormState(handle).update { edited }

        val savedState = parcelRoundTrip(handle.savedStateProvider().saveState())
        val restored = CreateFormState(SavedStateHandle.createHandle(savedState, null)).form.value

        assertEquals(edited.copy(location = null), restored.copy(location = null))
        assertEquals(49.0, restored.location!!.latitude, 0.0)
        assertEquals(8.4, restored.location.longitude, 0.0)
    }

    private fun parcelRoundTrip(bundle: Bundle): Bundle {
        val parcel = Parcel.obtain()
        try {
            bundle.writeToParcel(parcel, 0)
            parcel.setDataPosition(0)
            return Bundle.CREATOR.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}

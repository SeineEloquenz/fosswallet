package nz.eloque.foss_wallet.ui.screens.create

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassCreator
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import java.time.ZonedDateTime
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@HiltViewModel
class CreateViewModel @Inject constructor(
    application: Application,
    @param:ApplicationContext private val context: Context,
    private val passStore: PassStore,
) : AndroidViewModel(application) {

    data class GeocodeResult(
        val displayName: String,
        val latitude: Double,
        val longitude: Double,
    )

    fun passFlowById(passId: String): Flow<Pass?> {
        return passStore.passFlowById(passId).map { it?.pass }
    }

    suspend fun savePass(
        existingPass: Pass?,
        name: String,
        organization: String,
        serialNumber: String,
        type: PassType,
        barCodes: List<BarCode>,
        logoText: String,
        colors: PassColors?,
        location: Location?,
        relevantDates: List<PassRelevantDate>,
        expirationDate: ZonedDateTime?,
        iconUrl: Uri?,
        logoUrl: Uri?,
        stripUrl: Uri?,
        thumbnailUrl: Uri?,
        footerUrl: Uri?,
    ): String {
        val pass = createPass(
            existingPass = existingPass,
            name = name,
            organization = organization,
            serialNumber = serialNumber,
            type = type,
            barCodes = barCodes,
            logoText = logoText,
            colors = colors,
            location = location,
            relevantDates = relevantDates,
            expirationDate = expirationDate,
        )

        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.icon, null)!!
        val iconBitmap = loadBitmapFromUrl(context, iconUrl, ICON_SIZE) ?: drawableToBitmap(drawable, 64, 64)
        val logoBitmap = loadBitmapFromUrl(context, logoUrl, LOGO_SIZE)

        val finalLogo = when {
            existingPass != null -> logoBitmap
            logoBitmap != null -> logoBitmap
            iconUrl != null -> iconBitmap
            else -> drawableToBitmap(drawable, 256, 256)
        }

        val bitmaps = PassBitmaps(
            icon = iconBitmap,
            logo = finalLogo,
            strip = loadBitmapFromUrl(context, stripUrl, STRIP_SIZE),
            thumbnail = loadBitmapFromUrl(context, thumbnailUrl, THUMBNAIL_SIZE),
            footer = loadBitmapFromUrl(context, footerUrl, FOOTER_SIZE),
        )

        passStore.create(
            pass = pass.copy(
                hasLogo = bitmaps.logo != null,
                hasStrip = bitmaps.strip != null,
                hasThumbnail = bitmaps.thumbnail != null,
                hasFooter = bitmaps.footer != null,
            ),
            bitmaps = bitmaps
        )

        return pass.id
    }

    private fun createPass(
        existingPass: Pass?,
        name: String,
        organization: String,
        serialNumber: String,
        type: PassType,
        barCodes: List<BarCode>,
        logoText: String,
        colors: PassColors?,
        location: Location?,
        relevantDates: List<PassRelevantDate>,
        expirationDate: ZonedDateTime?,
    ): Pass {
        return if (existingPass == null) {
            PassCreator.create(
                name = name,
                type = type,
                barCodes = barCodes,
                organization = organization.ifBlank { PassCreator.ORGANIZATION },
                serialNumber = serialNumber.ifBlank { null },
                logoText = logoText.ifBlank { null },
                colors = colors,
                location = location,
                relevantDates = relevantDates,
                expirationDate = expirationDate,
            )!!
        } else {
            existingPass.copy(
                description = name,
                organization = organization,
                serialNumber = serialNumber,
                type = type,
                barCodes = LinkedHashSet(barCodes),
                logoText = logoText.ifBlank { null },
                colors = colors,
                locations = location?.let { listOf(it) } ?: emptyList(),
                relevantDates = relevantDates,
                expirationDate = expirationDate,
            )
        }
    }

    private suspend fun loadBitmapFromUrl(
        context: Context,
        imageUrl: Uri?,
        targetSize: Int,
    ): Bitmap? {
        if (imageUrl == null) return null

        if (imageUrl.scheme == "file") {
            return BitmapFactory.decodeFile(imageUrl.path)
        }

        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .precision(Precision.INEXACT)
            .scale(Scale.FIT)
            .size(targetSize)
            .data(imageUrl)
            .allowHardware(false) // IMPORTANT for Bitmap
            .build()

        val result = loader.execute(request)
        return if (result is SuccessResult) {
            (result.drawable as? BitmapDrawable)?.bitmap
        } else {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        private const val ICON_SIZE = 64
        private const val LOGO_SIZE = 512
        private const val STRIP_SIZE = 1024
        private const val THUMBNAIL_SIZE = 512
        private const val FOOTER_SIZE = 768
    }

    suspend fun geocode(query: String): List<GeocodeResult> {
        if (query.isBlank()) return emptyList()

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(query, 6, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        if (continuation.isActive) {
                            continuation.resume(addresses)
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) {
                            continuation.resume(emptyList())
                        }
                    }
                })
            }
        } else {
            geocoder.getFromLocationName(query, 6) ?: emptyList()
        }

        return addresses.map {
            val firstAddressLine = it.getAddressLine(0)
            GeocodeResult(
                displayName = firstAddressLine ?: listOfNotNull(it.featureName, it.locality, it.countryName)
                    .joinToString(", ")
                    .ifBlank { "Unknown" },
                latitude = it.latitude,
                longitude = it.longitude,
            )
        }
    }
}

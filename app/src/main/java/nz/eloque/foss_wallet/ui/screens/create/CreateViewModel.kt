package nz.eloque.foss_wallet.ui.screens.create

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.PassCreator
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import nz.eloque.foss_wallet.utils.toBitmap
import java.util.Locale
import kotlin.coroutines.resume

@HiltViewModel
class CreateViewModel
    @Inject
    constructor(
        application: Application,
        @param:ApplicationContext private val context: Context,
        private val passStore: PassStore,
        savedStateHandle: SavedStateHandle,
    ) : AndroidViewModel(application) {
        private val formState = CreateFormState(savedStateHandle)
        val form: StateFlow<CreateForm> = formState.form

        fun updateForm(transform: (CreateForm) -> CreateForm) = formState.update(transform)

        data class GeocodeResult(
            val displayName: String,
            val latitude: Double,
            val longitude: Double,
        )

        suspend fun savePass(): String {
            val form = form.value
            val pass =
                PassCreator.create(
                    name = form.name,
                    type = form.type,
                    barCodes = form.barcodes.map { it.toBarCode() },
                    organization = form.organization,
                    serialNumber = form.serialNumber,
                    colors = form.passColors(),
                    location = form.location,
                    relevantDates = form.relevantDates(),
                    expirationDate = form.expirationDate,
                )!!
            val iconUrl = form.iconUrl

            val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.icon, null)!!
            val iconBitmap = loadBitmapFromUrl(context, iconUrl, ICON_SIZE) ?: drawable.toBitmap(64, 64)
            val logoBitmap = loadBitmapFromUrl(context, form.logoUrl, LOGO_SIZE)

            val finalLogo =
                when {
                    logoBitmap != null -> logoBitmap
                    iconUrl != null -> iconBitmap
                    else -> drawable.toBitmap(256, 256)
                }

            val bitmaps =
                PassBitmaps(
                    icon = iconBitmap,
                    logo = finalLogo,
                    strip = loadBitmapFromUrl(context, form.stripUrl, STRIP_SIZE),
                    thumbnail = loadBitmapFromUrl(context, form.thumbnailUrl, THUMBNAIL_SIZE),
                    footer = loadBitmapFromUrl(context, form.footerUrl, FOOTER_SIZE),
                    background = loadBitmapFromUrl(context, form.backgroundUrl, BACKGROUND_SIZE),
                )

            passStore.create(
                pass =
                    pass.copy(
                        hasLogo = bitmaps.logo != null,
                        hasStrip = bitmaps.strip != null,
                        hasThumbnail = bitmaps.thumbnail != null,
                        hasFooter = bitmaps.footer != null,
                        hasBackground = bitmaps.background != null,
                    ),
                bitmaps = bitmaps,
            )

            return pass.id
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

            val request =
                ImageRequest
                    .Builder(context)
                    .precision(Precision.INEXACT)
                    .scale(Scale.FIT)
                    .size(targetSize)
                    .data(imageUrl)
                    .allowHardware(false) // IMPORTANT for Bitmap
                    // Picker previews in the shared cache would otherwise be accepted at the wrong size
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build()

            val result = context.imageLoader.execute(request)
            return if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
            } else {
                null
            }
        }

        companion object {
            private const val ICON_SIZE = 64
            private const val LOGO_SIZE = 512
            private const val STRIP_SIZE = 1024
            private const val THUMBNAIL_SIZE = 512
            private const val FOOTER_SIZE = 768
            private const val BACKGROUND_SIZE = 512
        }

        suspend fun geocode(query: String): List<GeocodeResult> {
            if (query.isBlank()) return emptyList()

            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocationName(
                            query,
                            6,
                            object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    if (continuation.isActive) {
                                        continuation.resume(addresses)
                                    }
                                }

                                override fun onError(errorMessage: String?) {
                                    if (continuation.isActive) {
                                        continuation.resume(emptyList())
                                    }
                                }
                            },
                        )
                    }
                } else {
                    @Suppress("DEPRECATION") // This blocks but is called from a coroutine, so thats fine
                    geocoder.getFromLocationName(query, 6) ?: emptyList()
                }

            return addresses.map {
                val firstAddressLine = it.getAddressLine(0)
                GeocodeResult(
                    displayName =
                        firstAddressLine ?: listOfNotNull(it.featureName, it.locality, it.countryName)
                            .joinToString(", ")
                            .ifBlank { "Unknown" },
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            }
        }
    }

package nz.eloque.foss_wallet.ui.screens.scan

import android.app.Application
import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.nielstron.bcbp.IataBcbp
import jakarta.inject.Inject
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassCreator
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps
import nz.eloque.foss_wallet.utils.linkifyUrls
import nz.eloque.foss_wallet.utils.toBitmap
import java.time.ZoneId
import java.time.format.FormatStyle

@HiltViewModel
class ScanViewModel
    @Inject
    constructor(
        application: Application,
        @param:ApplicationContext private val context: Context,
        private val passStore: PassStore,
    ) : AndroidViewModel(application) {
        suspend fun saveBcbpPass(
            barcode: BarCode,
            bcbp: IataBcbp.Parsed,
        ): String {
            val flightDate = bcbp.flightDate?.atStartOfDay(ZoneId.systemDefault())

            val headerFields =
                listOfNotNull(
                    plainField("group", "Grp", bcbp.checkInSequence),
                    plainField("seat", "Seat", bcbp.seat),
                )

            val primaryFields =
                listOfNotNull(
                    plainField("from", "From", bcbp.fromAirport),
                    plainField("to", "To", bcbp.toAirport),
                )

            val secondaryFields =
                listOfNotNull(
                    plainField("passenger", "Passenger", bcbp.passengerName),
                    plainField("class", "Class", bcbp.travelClass),
                    plainField("status", "Status", bcbp.passengerStatus),
                )

            val auxiliaryFields =
                listOfNotNull(
                    plainField("flight", "Flight", bcbp.flightCode()),
                    flightDate?.let { flightDate ->
                        PassField(
                            key = "date",
                            label = "DATE",
                            content =
                                PassContent.Date(
                                    date = flightDate,
                                    format = FormatStyle.MEDIUM,
                                    ignoresTimeZone = true,
                                    isRelative = false,
                                ),
                        )
                    },
                )

            val pass =
                PassCreator.create(
                    name = "Boarding Pass",
                    type = PassType.Boarding(TransitType.AIR),
                    barCode = barcode,
                    organization = bcbp.carrierCode,
                    serialNumber = bcbp.ticketIndicator,
                    relevantDates = flightDate?.let { listOf(PassRelevantDate.Date(it)) } ?: emptyList(),
                    expirationDate = flightDate?.plusDays(2),
                    headerFields = headerFields,
                    primaryFields = primaryFields,
                    secondaryFields = secondaryFields,
                    auxiliaryFields = auxiliaryFields,
                )!!

            val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.icon, null)!!

            val bitmaps =
                PassBitmaps(
                    icon = drawable.toBitmap(64, 64),
                    logo = null,
                    strip = null,
                    thumbnail = null,
                    footer = null,
                )

            passStore.create(
                pass =
                    pass.copy(
                        hasLogo = bitmaps.logo != null,
                        hasStrip = bitmaps.strip != null,
                        hasThumbnail = bitmaps.thumbnail != null,
                        hasFooter = bitmaps.footer != null,
                    ),
                bitmaps = bitmaps,
            )

            return pass.id
        }

        private fun plainField(
            key: String,
            label: String,
            value: String,
        ): PassField? {
            if (value.isBlank()) return null
            return PassField(
                key = key,
                label = label,
                content = PassContent.Plain(linkifyUrls(value)),
            )
        }
    }

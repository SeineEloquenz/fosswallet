package nz.eloque.foss_wallet.parsing

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.persistence.InvalidPassException
import nz.eloque.foss_wallet.persistence.PassBitmaps
import nz.eloque.foss_wallet.utils.forEach
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.Objects

class PassParser(val context: Context? = null) {

    fun parse(passJson: JSONObject, bitmaps: PassBitmaps, localizations: Set<PassLocalization>): Triple<Pass, PassBitmaps, Set<PassLocalization>> {
        if (!passJson.has("description")) {
            Log.w(TAG, "Pass has no description.")
            throw InvalidPassException()
        }
        val description = passJson.getString("description")
        val passVersion = passJson.optInt("formatVersion")
        if (passVersion != 1) {
            Log.w(TAG, "Pass has invalid formatVersion $passVersion")
            throw InvalidPassException()
        }
        if (!passJson.has("organizationName")) {
            Log.w(TAG, "Pass is missing organizationName.")
            throw InvalidPassException()
        }
        val organizationName = passJson.getString("organizationName")
        if (!passJson.has("serialNumber")) {
            Log.w(TAG, "Pass is missing serialNumber.")
            throw InvalidPassException()
        }
        val serialNumber = passJson.getString("serialNumber")
        return Triple(
            Pass(
                id = Objects.hash(serialNumber, organizationName).toLong(),
                description = description,
                formatVersion = passVersion,
                organization = organizationName,
                serialNumber = serialNumber,
                type = when {
                    passJson.has(PassType.EVENT) -> PassType.Event()
                    passJson.has(PassType.BOARDING) -> {
                        val boardingJson = passJson.getJSONObject(PassType.BOARDING)
                        val transitType = if (boardingJson.has("transitType")) { TransitType.fromName(boardingJson.getString("transitType")) } else { TransitType.GENERIC }
                        PassType.Boarding(transitType)
                    }
                    passJson.has(PassType.COUPON) -> PassType.Coupon()
                    passJson.has(PassType.STORE_CARD) -> PassType.StoreCard()
                    else -> PassType.Generic()
                },
                colors = parsePassColors(passJson),
                barCodes = parseBarcodes(passJson),
                hasLogo = bitmaps.logo != null,
                hasStrip = bitmaps.strip != null,
                hasThumbnail = bitmaps.thumbnail != null,
                hasFooter = bitmaps.footer != null,
                addedAt = Instant.now()
            ).also { pass ->
                pass.relevantDate = parseRelevantDate(passJson)
                pass.expirationDate = parseExpiration(passJson)
                pass.logoText = passJson.stringOrNull("logoText")
                pass.authToken = passJson.stringOrNull("authenticationToken")
                pass.webServiceUrl = passJson.stringOrNull("webServiceURL")
                pass.passTypeIdentifier = passJson.stringOrNull("passTypeIdentifier")
                if (passJson.has("locations")) {
                    passJson.getJSONArray("locations").forEach { locJson ->
                        pass.locations.add(Location("").also {
                            it.latitude = locJson.getDouble("latitude")
                            it.longitude = locJson.getDouble("longitude")
                        })
                    }
                }
                val fieldContainer = passJson.optJSONObject(pass.type.jsonKey)
                fieldContainer?.collectFields("headerFields", pass.headerFields)
                fieldContainer?.collectFields("primaryFields", pass.primaryFields)
                fieldContainer?.collectFields("secondaryFields", pass.secondaryFields)
                fieldContainer?.collectFields("auxiliaryFields", pass.auxiliaryFields)
                fieldContainer?.collectFields("backFields", pass.backFields)
            }, bitmaps, localizations
        )
    }

    private fun parseRelevantDate(passJson: JSONObject): Long {
        return try {
            if (passJson.has("relevantDate")) {
                ZonedDateTime.parse(passJson.stringOrNull("relevantDate") ?: EPOCH).toEpochSecond()
            } else {
                0L
            }
        } catch (e: DateTimeParseException) {
            Log.w(TAG, "Failed parsing relevantDate: $e")
            0L
        }
    }

    private fun parseExpiration(passJson: JSONObject): Long {
        return try {
            if (passJson.has("expirationDate")) {
                ZonedDateTime.parse(passJson.stringOrNull("expirationDate") ?: EPOCH).toEpochSecond()
            } else {
                0L
            }
        } catch(e: DateTimeParseException) {
            Log.w(TAG, "Failed parsing expirationDate: $e")
            0L
        }
    }

    private fun parseBarcodes(passJson: JSONObject): Set<BarCode> {
        val barcodes: MutableSet<BarCode> = LinkedHashSet()
        try {
            if (passJson.has("barcodes")) {
                passJson.getJSONArray("barcodes").forEach { codeJson ->
                    parseBarCode(codeJson)?.let { barcodes.add(it) }
                }
            } else if (passJson.has("barcode")) {
                parseBarCode(passJson.getJSONObject("barcode"))?.let { barcodes.add(it) }
            }
        } catch (e: JSONException) {
            Log.i(TAG, "Error parsing barcode json")
            Log.i(TAG, "Violating json: ${passJson.getJSONObject("barcode").toString(2)}")
            Log.i(TAG, "Exception: $e")
        }
        return barcodes
    }

    private fun parseBarCode(barcodeJSON: JSONObject): BarCode? {
        val barcodeFormatString = barcodeJSON.stringOrNull("type") ?: barcodeJSON.stringOrNull("format")
        return if (barcodeFormatString == null) {
            context?.let { Toast.makeText(it, it.getString(R.string.no_barcode_format_given), Toast.LENGTH_SHORT).show() }
            null
        } else {
            val barcodeFormat = BarCode.formatFromString(barcodeFormatString)
            BarCode(barcodeFormat, barcodeJSON.getString("message")).also {
                it.alternativeText = barcodeJSON.stringOrNull("altText")
            }
        }
    }

    private fun parsePassColors(passJson: JSONObject): PassColors? {
        val background = parseColor("backgroundColor", passJson)
        val foreground = parseColor("foregroundColor", passJson)
        val label = parseColor("labelColor", passJson)
        return if (background != null && foreground != null && label != null) {
            PassColors(background, foreground, label)
        } else null
    }

    private fun parseColor(key: String, passJson: JSONObject): Color? {
        return if (passJson.has(key)) {
            val representation = passJson.getString(key).filterNot { it.isWhitespace() }
            val regexResult = "rgb\\((\\d+),(\\d+),(\\d+)\\)".toRegex().find((representation))
            if (regexResult != null) {
                val (red, green, blue) = regexResult.destructured
                return Color(red.toInt(), green.toInt(), blue.toInt(), 255)
            } else null
        } else null
    }

    private fun JSONObject.collectFields(name: String, fieldContainer: MutableList<PassField>) {
        try {
            this.getJSONArray(name).forEach {
                fieldContainer.add(
                    PassField(
                        it.getString("key"),
                        it.getString("label"),
                        it.getString("value")
                    )
                )
            }
        } catch (e: JSONException) {
            Log.i(TAG, "Fields $name not existing. Stopping parsing.")
        }
    }

    private fun JSONObject.stringOrNull(key: String): String? {
        return if (this.has(key)) {
            this.getString(key)
        } else {
            null
        }
    }

    companion object {
        private const val TAG = "PassParser"
        private const val EPOCH = "1970-01-01T00:00:00Z"
    }
}
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
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.InvalidPassException
import nz.eloque.foss_wallet.persistence.PassBitmaps
import nz.eloque.foss_wallet.utils.Hash
import nz.eloque.foss_wallet.utils.forEach
import nz.eloque.foss_wallet.utils.map
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

class PassParser(val context: Context? = null) {

    fun parse(
        passJson: JSONObject,
        overridingId: String? = null,
        bitmaps: PassBitmaps,
        addedAt: Instant = Instant.now()
    ): Pass {
        if (!passJson.has("description")) {
            Log.w(TAG, context.getString(R.string.no_description))
            throw InvalidPassException()
        }
        val description = passJson.getString("description")
        val passVersion = passJson.optInt("formatVersion")
        if (passVersion != 1) {
            Log.w(TAG, context.getString(R.string.invalid_format_version))
            throw InvalidPassException()
        }
        if (!passJson.has("organizationName")) {
            Log.w(TAG, context.getString(R.string.missing_organization_name))
            throw InvalidPassException()
        }
        val organizationName = passJson.getString("organizationName")
        if (!passJson.has("serialNumber")) {
            Log.w(TAG, context.getString(R.string.missing_serial_number))
            throw InvalidPassException()
        }
        val serialNumber = passJson.getString("serialNumber")
        val type = when {
            passJson.has(PassType.EVENT) -> PassType.Event()
            passJson.has(PassType.BOARDING) -> {
                val boardingJson = passJson.getJSONObject(PassType.BOARDING)
                val transitType = if (boardingJson.has("transitType")) { TransitType.fromName(boardingJson.getString("transitType")) } else { TransitType.GENERIC }
                PassType.Boarding(transitType)
            }
            passJson.has(PassType.COUPON) -> PassType.Coupon()
            passJson.has(PassType.STORE_CARD) -> PassType.StoreCard()
            else -> PassType.Generic()
        }

        val locations = if (passJson.has("locations")) {
            passJson.getJSONArray("locations").map { locJson ->
                Location("").also {
                    it.latitude = locJson.getDouble("latitude")
                    it.longitude = locJson.getDouble("longitude")
                }
            }
        } else listOf()

        val fieldContainer = passJson.optJSONObject(type.jsonKey)

        return Pass(
            id = overridingId ?: Hash.sha256(passJson.toString()),
            description = description,
            formatVersion = passVersion,
            organization = organizationName,
            serialNumber = serialNumber,
            type = type,
            colors = parsePassColors(passJson),
            barCodes = parseBarcodes(passJson),
            hasLogo = bitmaps.logo != null,
            hasStrip = bitmaps.strip != null,
            hasThumbnail = bitmaps.thumbnail != null,
            hasFooter = bitmaps.footer != null,
            addedAt = addedAt,
            relevantDate = parseRelevantDate(passJson),
            expirationDate = parseExpiration(passJson),
            logoText = passJson.stringOrNull("logoText"),
            authToken = passJson.stringOrNull("authenticationToken"),
            webServiceUrl = passJson.stringOrNull("webServiceURL"),
            passTypeIdentifier = passJson.stringOrNull("passTypeIdentifier"),
            locations = locations,
            headerFields = fieldContainer?.collectFields("headerFields")?:listOf(),
            primaryFields = fieldContainer?.collectFields("primaryFields")?:listOf(),
            secondaryFields = fieldContainer?.collectFields("secondaryFields")?:listOf(),
            auxiliaryFields = fieldContainer?.collectFields("auxiliaryFields")?:listOf(),
            backFields = fieldContainer?.collectFields("backFields")?:listOf()
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
            Log.w(TAG, context.getString(R.string.failed_relevant_date))
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
            Log.w(TAG, context.getString(R.string.failed_expiration_date))
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
            Log.e(TAG, "Error parsing barcode json")
            Log.e(TAG, "Violating json: ${passJson.getJSONObject("barcode").toString(2)}")
            Log.e(TAG, context.getString(R.string.exception))
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
            BarCode(
                barcodeFormat,
                barcodeJSON.getString("message"),
                Charset.forName(barcodeJSON.optString("messageEncoding", BarCode.FALLBACK_CHARSET.toString())),
                barcodeJSON.stringOrNull("altText")
            )
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

    private fun JSONObject.collectFields(name: String): List<PassField>? {
        return try {
            this.getJSONArray(name).map { FieldParser.parse(it) }
        } catch (_: JSONException) {
            Log.e(TAG, context.getString(R.string.fields_not_exist))
            null
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

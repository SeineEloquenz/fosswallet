package nz.eloque.foss_wallet.persistence

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import android.widget.Toast
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.utils.forEach
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.zip.ZipInputStream

class InvalidPassException : Exception()

class PassBitmaps(
    val icon: Bitmap,
    val logo: Bitmap?,
    val strip: Bitmap?,
    val thumbnail: Bitmap?,
    val footer: Bitmap?
) {

    fun saveToDisk(context: Context, id: Long) {
        val directory = File(context.filesDir, "$id")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        save(directory, "icon.png", icon)
        save(directory, "logo.png", logo)
        save(directory, "strip.png", strip)
        save(directory, "thumbnail.png", thumbnail)
        save(directory, "footer.png", footer)
    }

    private fun save(directory: File, path: String, bitmap: Bitmap?) {
        bitmap?.let {
            FileOutputStream(File(directory, path)).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}

class PassLoader(
    private val context: Context
) {

    fun load(inputStream: InputStream): Pair<Pass, PassBitmaps> {
        var passJson: JSONObject? = null
        var logo: Bitmap? = null
        var icon: Bitmap? = null
        var strip: Bitmap? = null
        var thumbnail: Bitmap? = null
        var footer: Bitmap? = null
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry

            while (zip.nextEntry.also { entry = it } != null) {
                if (!entry!!.isDirectory) {
                    Log.d(TAG, "Found file: ${entry.name}")
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    val baos = ByteArrayOutputStream()

                    while (zip.read(buffer).also { bytesRead = it } != -1) {
                        baos.write(buffer, 0, bytesRead)
                    }
                    when (entry.name) {
                        "pass.json" -> {
                            val content = baos.toString("UTF-8")
                            passJson = JSONObject(content)
                            println("Content:\n$content")
                        }
                        "logo.png", "logo@2x.png" -> {
                            logo = loadImage(baos)
                        }
                        "icon.png", "icon@2x.png" -> {
                            icon = loadImage(baos)
                        }
                        "strip.png", "strip@2x.png" -> {
                            strip = loadImage(baos)
                        }
                        "thumbnail.png", "thumbnail@2x.png" -> {
                            thumbnail = loadImage(baos)
                        }
                        "footer.png", "footer@2x.png" -> {
                            footer = loadImage(baos)
                        }
                    }
                }
            }
        }
        if (icon == null) {
            icon = logo ?: Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
        //TODO check signature before returning
        if (passJson != null) {
            val bitmaps = PassBitmaps(icon!!, logo, strip, thumbnail, footer)
            return parse(passJson!!, bitmaps)
        } else {
            throw InvalidPassException()
        }
    }

    private fun loadImage(baos: ByteArrayOutputStream): Bitmap? {
        val array = baos.toByteArray()
        val image = BitmapFactory.decodeByteArray(array, 0, array.size)
        return if (image == null) {
            Log.w(TAG, "Failed parsing image from pkpass! Is it missing?")
            null
        } else {
            image
        }
    }


    private fun parse(passJson: JSONObject, bitmaps: PassBitmaps): Pair<Pass, PassBitmaps> {
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
        val organizationName = passJson.optString("organizationName")
        if (!passJson.has("serialNumber")) {
            Log.w(TAG, "Pass is missing serialNumber.")
            throw InvalidPassException()
        }
        val serialNumber = passJson.optString("serialNumber")
        return Pair(
            Pass(
                description = description,
                formatVersion = passVersion,
                organization = organizationName,
                serialNumber = serialNumber,
                type = when {
                    passJson.has("eventTicket") -> PassType.EVENT
                    passJson.has("boardingPass") -> PassType.BOARDING
                    passJson.has("coupon") -> PassType.COUPON
                    else -> PassType.GENERIC
                },
                barCodes = parseBarcodes(passJson),
                hasLogo = bitmaps.logo != null,
                hasStrip = bitmaps.strip != null,
                hasThumbnail = bitmaps.thumbnail != null,
                hasFooter = bitmaps.footer != null,
            ).also { pass ->
                pass.relevantDate = parseRelevantDate(passJson)
                pass.expirationDate = parseExpiration(passJson)
                pass.logoText = passJson.optString("logoText")
                pass.authToken = passJson.optString("authToken")
                pass.webServiceUrl = passJson.optString("webServiceUrl")
                pass.passIdent = passJson.optString("passIdent")
                if (passJson.has("locations")) {
                    passJson.getJSONArray("locations").forEach { locJson ->
                        pass.locations.add(Location("").also {
                            it.latitude = locJson.getDouble("latitude")
                            it.longitude = locJson.getDouble("longitude")
                        })
                    }
                }
                val fieldContainer = passJson.getJSONObject(pass.type.jsonKey)
                collectFields(fieldContainer, "headerFields", pass.headerFields)
                collectFields(fieldContainer, "primaryFields", pass.primaryFields)
                collectFields(fieldContainer, "secondaryFields", pass.secondaryFields)
                collectFields(fieldContainer, "auxiliaryFields", pass.auxiliaryFields)
                collectFields(fieldContainer, "backFields", pass.backFields)
            }, bitmaps
        )
    }

    private fun parseRelevantDate(passJson: JSONObject): Long {
        return try {
            if (passJson.has("relevantDate")) {
                ZonedDateTime.parse(passJson.optString("relevantDate") ?: EPOCH).toEpochSecond()
            } else if(passJson.has("when")) {
                ZonedDateTime.parse(passJson.getJSONObject("when").optString("dateTime") ?: EPOCH).toEpochSecond()
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
                ZonedDateTime.parse(passJson.optString("expirationDate") ?: EPOCH).toEpochSecond()
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
            if (passJson.has("barcode")) {
                parseBarCode(passJson.getJSONObject("barcode"))?.let { barcodes.add(it) }
            }
            if (passJson.has("barcodes")) {
                passJson.getJSONArray("barcodes").forEach { codeJson ->
                    parseBarCode(codeJson)?.let { barcodes.add(it) }
                }
            }
        } catch (e: JSONException) {
            Log.i(TAG, "Error parsing barcode json")
            Log.i(TAG, "Violating json: ${passJson.getJSONObject("barcode").toString(2)}")
            Log.i(TAG, "Exception: $e")
        }
        return barcodes
    }

    private fun parseBarCode(barcodeJSON: JSONObject): BarCode? {
        val barcodeFormatString = barcodeJSON.optString("type") ?: barcodeJSON.optString("format")
        return if (barcodeFormatString == null) {
            Toast.makeText(context, context.getString(R.string.no_barcode_format_given), Toast.LENGTH_SHORT).show()
            null
        } else {
            val barcodeFormat = BarCode.formatFromString(barcodeFormatString)
            BarCode(barcodeFormat, barcodeJSON.getString("message")).also {
                it.alternativeText = barcodeJSON.optString("altText")
            }
        }
    }

    private fun collectFields(json: JSONObject, name: String, fieldContainer: MutableList<PassField>) {
        try {
            json.getJSONArray(name).forEach {
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

    companion object {
        private const val TAG = "PassLoader"
        private const val EPOCH = "1970-01-01T00:00:00Z"
    }
}

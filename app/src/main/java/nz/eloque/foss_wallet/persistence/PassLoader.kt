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
import nz.eloque.foss_wallet.utils.forEach
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.zip.ZipInputStream

class InvalidPassException : Exception()

private class RawPass(
    val passJson: JSONObject,
    val icon: Bitmap,
    val logo: Bitmap?,
    val strip: Bitmap?,
    val thumbnail: Bitmap?,
    val footer: Bitmap?)

class PassLoader(
    private val context: Context
) {

    fun load(inputStream: InputStream): Pass {
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
            return fromRaw(RawPass(passJson!!, icon!!, logo, strip, thumbnail, footer))
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


    private fun fromRaw(rawPass: RawPass): Pass {
        val description = rawPass.passJson.optString("description")
            ?: rawPass.passJson.optJSONObject("what")?.optString("description")
            ?: "No description"

        return Pass(
            description = description,
            icon = rawPass.icon,
            barCodes = parseBarcodes(rawPass.passJson)
        ).also { pass ->
            pass.organization = rawPass.passJson.optString("organizationName")
            pass.serialNumber = rawPass.passJson.optString("serialNumber")
            pass.relevantDate = parseRelevantDate(rawPass.passJson)
            pass.expirationDate = parseExpiration(rawPass.passJson)
            pass.authToken = rawPass.passJson.optString("authToken")
            pass.webServiceUrl = rawPass.passJson.optString("webServiceUrl")
            pass.passIdent = rawPass.passJson.optString("passIdent")
            pass.logo = rawPass.logo
            pass.strip = rawPass.strip
            pass.thumbnail = rawPass.thumbnail
            pass.footer = rawPass.footer
            if (rawPass.passJson.has("locations")) {
                rawPass.passJson.getJSONArray("locations").forEach { locJson ->
                    pass.locations.add(Location("").also {
                        it.latitude = locJson.getDouble("latitude")
                        it.longitude = locJson.getDouble("longitude")
                    })
                }
            }
            val fieldContainer = rawPass.passJson.getJSONObject(when {
                rawPass.passJson.has("eventTicket") -> "eventTicket"
                else -> "generic"
            })
            collectFields(fieldContainer, "headerFields", pass.headerFields)
            collectFields(fieldContainer, "primaryFields", pass.primaryFields)
            collectFields(fieldContainer, "secondaryFields", pass.secondaryFields)
            collectFields(fieldContainer, "auxiliaryFields", pass.auxiliaryFields)
            collectFields(fieldContainer, "backFields", pass.backFields)
        }
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

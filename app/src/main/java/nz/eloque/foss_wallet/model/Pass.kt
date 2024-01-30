package nz.eloque.foss_wallet.model

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.ZonedDateTime
import java.util.LinkedList


data class PassField(val key: String, val label: String, val value: String)

class Pass(
    val description: String,
    val icon: Bitmap,
) {
    var organization: String? = null
    var type: PassType = PassType.EVENT
    var barCode: BarCode? = null
    var serialNumber: String? = null
    var logo: Bitmap? = null
    var strip: Bitmap? = null
    var footer: Bitmap? = null
    val locations: MutableList<Location> = ArrayList()
    val headerFields: MutableList<PassField> = LinkedList()
    val primaryFields: MutableList<PassField> = LinkedList()
    val secondaryFields: MutableList<PassField> = LinkedList()
    val auxiliaryFields: MutableList<PassField> = LinkedList()
    val backFields: MutableList<PassField> = LinkedList()

    companion object {

        private const val TAG = "Pass"

        fun from(rawPass: RawPass): Pass {
            val description = if (rawPass.passJson.has("description")) {
                rawPass.passJson.getString("description")
            } else if (rawPass.passJson.has("what")) {
                val whatJSON = rawPass.passJson.getJSONObject("what")
                whatJSON.getString("description")
            } else {
                "No description given"
            }

            return Pass(
                description,
                rawPass.icon
            ).also { pass ->
                pass.organization = rawPass.passJson.getString("organizationName")
                pass.serialNumber = rawPass.passJson.getString("serialNumber")
                pass.barCode = parseBarcode(rawPass.passJson)
                pass.logo = rawPass.logo
                pass.strip = rawPass.strip
                pass.footer = rawPass.footer
                if (rawPass.passJson.has("locations")) {
                    forEach(rawPass.passJson.getJSONArray("locations")) { locJson ->
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

        private fun parseBarcode(passJson: JSONObject): BarCode? {
            return try {
                if (passJson.has("barcode")) {
                    val barcodeJSON = passJson.getJSONObject("barcode")
                    val barcodeFormatString = if (barcodeJSON.has("type")) {
                        barcodeJSON.getString("type")
                    } else {
                        barcodeJSON.getString("format")
                    }

                    val barcodeFormat = BarCode.getFormatFromString(barcodeFormatString)
                    val barCode = BarCode(barcodeFormat, barcodeJSON.getString("message"))
                    if (barcodeJSON.has("altText")) {
                        barCode.alternativeText = barcodeJSON.getString("altText")
                    }
                    barCode
                } else {
                    null
                }
            } catch (e: JSONException) {
                Log.i(TAG, "Error parsing barcode json")
                Log.i(TAG, "Violating json: ${passJson.getJSONObject("barcode").toString(2)}")
                Log.i(TAG, "Exception: $e")
                null
            }
        }

        private fun collectFields(json: JSONObject, name: String, fieldContainer: MutableList<PassField>) {
            try {
                forEach(json.getJSONArray(name)) {
                    fieldContainer.add(PassField(
                        it.getString("key"),
                        it.getString("label"),
                        it.getString("value")
                    ))
                }
            } catch (e: JSONException) {
                Log.i(TAG, "Fields $name not existing. Stopping parsing.")
            }
        }

        private fun forEach(jsonArray: JSONArray, action: (JSONObject) -> Unit) {
            var i = 0
            while (i < jsonArray.length()) {
                val element = jsonArray.getJSONObject(i)
                action.invoke(element)
                i++
            }
        }
    }
}
package nz.eloque.foss_wallet.model

import android.util.Log
import java.io.File
import java.time.ZonedDateTime

object PassReader {

    const val TAG = "PassReader"

    fun read(path: File): Pass {

        val pass = Pass(path.name)

        val file = File(path, "data.json")

        try {
            val plainJsonString = file.bufferedReader().readText()
            val passJSON = readJSONSafely(plainJsonString)!!

            if (passJSON.has("what")) {
                val whatJSON = passJSON.getJSONObject("what")
                pass.description = whatJSON.getString("description")
            }

            if (passJSON.has("meta")) {
                val metaJSON = passJSON.getJSONObject("meta")
                pass.type = TYPE_MAP[metaJSON.getString("type")] ?: PassType.GENERIC
                pass.creator = metaJSON.getString("organisation")
                pass.app = metaJSON.getString("app")
            }

            if (passJSON.has("barcode")) {
                val barcodeJSON = passJSON.getJSONObject("barcode")
                val barcodeFormatString = barcodeJSON.getString("type")

                val barcodeFormat = BarCode.getFormatFromString(barcodeFormatString)
                val barCode = BarCode(barcodeFormat, barcodeJSON.getString("message"))
                pass.barCode = barCode

                if (barcodeJSON.has("altText")) {
                    barCode.alternativeText = barcodeJSON.getString("altText")
                }
            }

            if (passJSON.has("when")) {
                val dateTime = passJSON.getJSONObject("when").getString("dateTime")

                pass.calendarTimespan = Pass.TimeSpan()
                pass.calendarTimespan = Pass.TimeSpan(from = ZonedDateTime.parse(dateTime))
            }

        } catch (e: Exception) {
            Log.i(TAG, "PassParse Exception: $e")
        }

        return pass
    }

}
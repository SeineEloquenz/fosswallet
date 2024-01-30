package nz.eloque.foss_wallet.model

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import nz.eloque.foss_wallet.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.LinkedList


data class PassField(val key: String, val label: String, val value: String)

class Pass(
    val description: String,
    val icon: Bitmap,
    val barCodes: Set<BarCode>
) {
    var organization: String? = null
    var type: PassType = PassType.EVENT
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
    }
}
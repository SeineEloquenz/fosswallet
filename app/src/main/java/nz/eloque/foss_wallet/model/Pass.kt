package nz.eloque.foss_wallet.model

import android.location.Location
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime


data class PassField(val key: String, private val label: String, private val value: String, val hide: Boolean, val hint:String = "")

fun fromRaw(rawPass: RawPass): Pass {
    return Pass().also { pass ->
        pass.organization = rawPass.passJson.getString("organizationName")
        pass.description = rawPass.passJson.getString("description")
        pass.serialNumber = rawPass.passJson.getString("serialNumber")
        forEach(rawPass.passJson.getJSONArray("locations")) { locJson ->
            pass.locations.add(Location("").also {
                it.latitude = locJson.getDouble("latitude")
                it.longitude = locJson.getDouble("longitude")
            })
        }
    }
}

fun forEach(jsonArray: JSONArray, action: (JSONObject) -> Unit) {
    var i = 0
    while (i < jsonArray.length()) {
        val element = jsonArray.getJSONObject(i)
        action.invoke(element)
        i++
    }
}

class Pass {
    var organization: String? = null
    var type: PassType = PassType.EVENT
    var barCode: BarCode? = null
    var description: String? = null
    var serialNumber: String? = null
    class TimeSpan(val from: ZonedDateTime? = null, val to: ZonedDateTime? = null)
    var fields: MutableList<PassField> = ArrayList()
    var locations: MutableList<Location> = ArrayList()
    var app: String? = null
    var authToken: String? = null
    var webServiceURL: String? = null
}
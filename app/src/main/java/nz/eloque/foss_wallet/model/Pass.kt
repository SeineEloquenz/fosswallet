package nz.eloque.foss_wallet.model

import android.location.Location
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime
import java.util.LinkedList


data class PassField(val key: String, private val label: String, private val value: String)

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
        collectFields(rawPass.passJson.getJSONArray("headerFields"), pass.headerFields)
        collectFields(rawPass.passJson.getJSONArray("primaryFields"), pass.primaryFields)
        collectFields(rawPass.passJson.getJSONArray("secondaryFields"), pass.secondaryFields)
        collectFields(rawPass.passJson.getJSONArray("auxiliaryFields"), pass.auxiliaryFields)
        collectFields(rawPass.passJson.getJSONArray("backFields"), pass.backFields)
    }
}

fun collectFields(jsonArray: JSONArray, fieldContainer: MutableList<PassField>) {
    forEach(jsonArray) {
        fieldContainer.add(PassField(
            it.getString("key"),
            it.getString("label"),
            it.getString("value")
        ))
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
    var locations: MutableList<Location> = ArrayList()
    val headerFields: MutableList<PassField> = LinkedList()
    val primaryFields: MutableList<PassField> = LinkedList()
    val secondaryFields: MutableList<PassField> = LinkedList()
    val auxiliaryFields: MutableList<PassField> = LinkedList()
    val backFields: MutableList<PassField> = LinkedList()
}
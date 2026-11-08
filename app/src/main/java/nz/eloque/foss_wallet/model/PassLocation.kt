package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.utils.doubleOrNull
import nz.eloque.foss_wallet.utils.stringOrNull
import org.json.JSONObject

data class PassLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val relevantText: String? = null,
) {
    fun toJson(): JSONObject =
        JSONObject().also {
            it.put("latitude", latitude)
            it.put("longitude", longitude)
            altitude?.let { altitude -> it.put("altitude", altitude) }
            relevantText?.let { text -> it.put("relevantText", text) }
        }

    companion object {
        fun fromJson(json: JSONObject): PassLocation =
            PassLocation(
                latitude = json.getDouble("latitude"),
                longitude = json.getDouble("longitude"),
                altitude = json.doubleOrNull("altitude"),
                relevantText = json.stringOrNull("relevantText"),
            )
    }
}

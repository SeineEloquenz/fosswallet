package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.utils.stringOrNull
import org.json.JSONObject

data class PassBeacon(
    val proximityUUID: String,
    val major: Int? = null,
    val minor: Int? = null,
    val relevantText: String? = null,
) {
    fun toJson(): JSONObject =
        JSONObject().also {
            it.put("proximityUUID", proximityUUID)
            major?.let { major -> it.put("major", major) }
            minor?.let { minor -> it.put("minor", minor) }
            relevantText?.let { text -> it.put("relevantText", text) }
        }

    companion object {
        fun fromJson(json: JSONObject): PassBeacon =
            PassBeacon(
                proximityUUID = json.getString("proximityUUID"),
                major = if (json.isNull("major")) null else json.getInt("major"),
                minor = if (json.isNull("minor")) null else json.getInt("minor"),
                relevantText = json.stringOrNull("relevantText"),
            )
    }
}

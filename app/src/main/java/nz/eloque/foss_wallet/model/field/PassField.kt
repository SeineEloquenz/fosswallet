package nz.eloque.foss_wallet.model.field

import androidx.room.Entity
import nz.eloque.foss_wallet.utils.inIgnoreCase
import org.json.JSONObject


@Entity
data class PassField(
    val key: String,
    val label: String,
    val content: PassContent,
) {
    fun toJson(): JSONObject {
        return JSONObject().also {
            it.put("key", key)
            it.put("label", label)
            it.put("value", content.serialize())
        }
    }

    fun contains(query: String): Boolean {
        return query inIgnoreCase this.label || this.content.contains(query)
    }

    companion object {
        fun fromJson(json: JSONObject): PassField {
            return PassField(json.getString("key"), json.getString("label"), PassContent.deserialize(json.getString("value")))
        }
    }
}
package nz.eloque.foss_wallet.model.field

import androidx.room.Entity
import nz.eloque.foss_wallet.utils.inIgnoreCase
import nz.eloque.foss_wallet.utils.stringOrNull
import org.json.JSONObject

@Entity
data class PassField(
    val key: String,
    val label: String?,
    val content: PassContent,
    val changeMessage: String? = null,
) {
    fun toJson(): JSONObject =
        JSONObject().also { json ->
            json.put("key", key)
            json.put("label", label)
            json.put("value", content.serialize())
            changeMessage?.let { json.put("changeMessage", it) }
        }

    fun hasChangeMessage(): Boolean = changeMessage != null

    fun contains(query: String): Boolean = query inIgnoreCase this.label || this.content.contains(query)

    companion object {
        fun fromJson(json: JSONObject): PassField =
            PassField(
                json.getString("key"),
                json.stringOrNull("label"),
                PassContent.deserialize(json.getString("value")),
                if (json.has("changeMessage")) json.getString("changeMessage") else null,
            )

        val Empty = PassField("", "", PassContent.Plain(""))
    }
}

fun PassField?.isNotEmpty(): Boolean = this != null && this.content.isNotEmpty()

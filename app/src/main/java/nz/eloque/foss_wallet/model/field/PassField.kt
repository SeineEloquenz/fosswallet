package nz.eloque.foss_wallet.model.field

import androidx.room3.Entity
import nz.eloque.foss_wallet.utils.inIgnoreCase
import nz.eloque.foss_wallet.utils.stringOrNull
import org.json.JSONObject

@Entity
data class PassField(
    val key: String,
    val label: String?,
    val content: PassContent,
    val changeMessage: String? = null,
    val textAlignment: TextAlignment? = null,
    /** Null means the default detectors, an empty set disables detection. */
    val dataDetectorTypes: Set<DataDetectorType>? = null,
    val row: Int? = null,
) {
    fun toJson(): JSONObject =
        JSONObject().also { json ->
            json.put("key", key)
            json.put("label", label)
            json.put("value", content.serialize())
            changeMessage?.let { json.put("changeMessage", it) }
            textAlignment?.let { json.put("textAlignment", it.jsonValue) }
            dataDetectorTypes?.let { json.put("dataDetectorTypes", DataDetectorType.toJson(it)) }
            row?.let { json.put("row", it) }
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
                json.textAlignmentOrNull(),
                json.dataDetectorTypesOrNull(),
                json.rowOrNull(),
            )

        fun JSONObject.textAlignmentOrNull(): TextAlignment? = stringOrNull("textAlignment")?.let { TextAlignment.fromJson(it) }

        fun JSONObject.dataDetectorTypesOrNull(): Set<DataDetectorType>? =
            optJSONArray("dataDetectorTypes")?.let { DataDetectorType.fromJson(it) }

        fun JSONObject.rowOrNull(): Int? = if (isNull("row")) null else optInt("row")

        val Empty = PassField("", "", PassContent.Plain(""))
    }
}

fun PassField?.isNotEmpty(): Boolean = this != null && this.content.isNotEmpty()

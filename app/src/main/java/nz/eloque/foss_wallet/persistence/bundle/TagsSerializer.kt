package nz.eloque.foss_wallet.persistence.bundle

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import nz.eloque.foss_wallet.model.Tag
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializes the tags assigned to passes into the json sidecar entry of a
 * .pkpasses bundle, so backups can restore tag assignments.
 */
object TagsSerializer {
    const val BUNDLE_ENTRY = "tags.json"

    fun serialize(tagsByPass: Map<String, Set<Tag>>): String {
        val passes = JSONObject()
        tagsByPass.filterValues { it.isNotEmpty() }.forEach { (passId, tags) ->
            val tagArray = JSONArray()
            tags.forEach { tag ->
                tagArray.put(
                    JSONObject()
                        .put("label", tag.label)
                        .put("color", tag.color.toArgb()),
                )
            }
            passes.put(passId, tagArray)
        }
        return JSONObject().put("passes", passes).toString()
    }

    fun deserialize(content: String): Map<String, Set<Tag>> {
        val result = mutableMapOf<String, Set<Tag>>()
        val passes = JSONObject(content).optJSONObject("passes") ?: return result
        passes.keys().forEach { passId ->
            val tagArray = passes.getJSONArray(passId)
            val tags = mutableSetOf<Tag>()
            for (i in 0 until tagArray.length()) {
                val tagJson = tagArray.getJSONObject(i)
                tags.add(Tag(tagJson.getString("label"), Color(tagJson.getInt("color"))))
            }
            result[passId] = tags
        }
        return result
    }
}

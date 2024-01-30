package nz.eloque.foss_wallet

import org.json.JSONArray
import org.json.JSONObject

fun forEach(jsonArray: JSONArray, action: (JSONObject) -> Unit) {
    var i = 0
    while (i < jsonArray.length()) {
        val element = jsonArray.getJSONObject(i)
        action.invoke(element)
        i++
    }
}
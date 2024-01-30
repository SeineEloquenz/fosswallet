package nz.eloque.foss_wallet.utils

import org.json.JSONArray
import org.json.JSONObject

fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    var i = 0
    while (i < this.length()) {
        val element = this.getJSONObject(i)
        action.invoke(element)
        i++
    }
}
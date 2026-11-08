package nz.eloque.foss_wallet.model.field

import org.json.JSONArray

enum class DataDetectorType(
    val jsonValue: String,
) {
    PHONE_NUMBER("PKDataDetectorTypePhoneNumber"),
    LINK("PKDataDetectorTypeLink"),
    ADDRESS("PKDataDetectorTypeAddress"),
    CALENDAR_EVENT("PKDataDetectorTypeCalendarEvent"),
    ;

    companion object {
        fun fromJson(value: String): DataDetectorType? = entries.firstOrNull { it.jsonValue == value }

        fun fromJson(values: JSONArray): Set<DataDetectorType> =
            (0 until values.length()).mapNotNull { fromJson(values.getString(it)) }.toSet()

        fun toJson(types: Set<DataDetectorType>): JSONArray = JSONArray(types.map { it.jsonValue })
    }
}

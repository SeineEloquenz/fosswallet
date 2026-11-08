package nz.eloque.foss_wallet.persistence

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room3.ColumnTypeConverter
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassBeacon
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassLocation
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.parsing.TimeParser
import nz.eloque.foss_wallet.utils.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.UUID

class TypeConverters {
    @ColumnTypeConverter
    fun fromZonedDateTime(dateTime: ZonedDateTime): String = dateTime.toString()

    @ColumnTypeConverter
    fun toZonedDateTime(dateTime: String): ZonedDateTime =
        TimeParser.parseAbsoluteOrNull(dateTime) ?: throw DateTimeParseException("Not a timestamp: $dateTime", dateTime, 0)

    @ColumnTypeConverter
    fun fromRelevantDates(relevantDates: List<PassRelevantDate>): String {
        val json = JSONArray()
        relevantDates.forEach {
            val dJson = JSONObject()
            if (it is PassRelevantDate.Date) {
                dJson.put("date", it.date.toString())
            } else if (it is PassRelevantDate.DateInterval) {
                dJson.put("startDate", it.startDate.toString())
                dJson.put("endDate", it.endDate.toString())
            }
            json.put(dJson)
        }
        return json.toString()
    }

    @ColumnTypeConverter
    fun toRelevantDates(str: String): List<PassRelevantDate> =
        JSONArray(str).map {
            if (it.has("date")) {
                PassRelevantDate.Date(
                    toZonedDateTime(it.getString("date")),
                )
            } else {
                PassRelevantDate.DateInterval(
                    toZonedDateTime(it.getString("startDate")),
                    toZonedDateTime(it.getString("endDate")),
                )
            }
        }

    @ColumnTypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @ColumnTypeConverter
    fun toInstant(instant: Long): Instant = Instant.ofEpochMilli(instant)

    @ColumnTypeConverter
    fun fromColors(colors: PassColors): String = "${colors.background.toArgb()},${colors.foreground.toArgb()},${colors.label.toArgb()}"

    @ColumnTypeConverter
    fun toColors(colors: String): PassColors {
        val split = colors.split(",")
        return PassColors(Color(split[0].toInt()), Color(split[1].toInt()), Color(split[2].toInt()))
    }

    @ColumnTypeConverter
    fun fromColor(color: Color): String = color.toArgb().toString()

    @ColumnTypeConverter
    fun toColor(color: String): Color = Color(color.toInt())

    @ColumnTypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    @ColumnTypeConverter
    fun toUuid(uuid: String): UUID = UUID.fromString(uuid)

    @ColumnTypeConverter
    fun fromPassType(passType: PassType): String =
        when (passType) {
            is PassType.Boarding -> passType.jsonKey + "," + passType.transitType.toString()
            is PassType.Coupon -> passType.jsonKey
            is PassType.Event -> passType.jsonKey
            is PassType.Generic -> passType.jsonKey
            is PassType.StoreCard -> passType.jsonKey
        }

    @ColumnTypeConverter
    fun toPassType(passType: String): PassType {
        val split = passType.split(",")
        return if (split.size > 1) {
            PassType.Boarding(TransitType.valueOf(split[1]))
        } else {
            when (passType) {
                PassType.EVENT -> PassType.Event
                PassType.COUPON -> PassType.Coupon
                PassType.STORE_CARD -> PassType.StoreCard
                else -> PassType.Generic
            }
        }
    }

    @ColumnTypeConverter
    fun fromLocations(locations: List<PassLocation>): String = JSONArray(locations.map { it.toJson() }).toString()

    @ColumnTypeConverter
    fun toLocations(str: String): List<PassLocation> = JSONArray(str).map { PassLocation.fromJson(it) }

    @ColumnTypeConverter
    fun fromBarcodes(barcodes: Set<BarCode>): String {
        val json = JSONArray()
        barcodes.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @ColumnTypeConverter
    fun toBarcodes(str: String): Set<BarCode> {
        // Preserve barcode order from JSON so first barcode remains stable in UI/edit flows.
        return JSONArray(str)
            .map { BarCode.fromJson(it) }
            .toCollection(LinkedHashSet())
    }

    @ColumnTypeConverter
    fun fromFields(fields: List<PassField>): String {
        val json = JSONArray()
        fields.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @ColumnTypeConverter
    fun toFields(str: String): List<PassField> = JSONArray(str).map { PassField.fromJson(it) }

    @ColumnTypeConverter
    fun fromBeacons(beacons: List<PassBeacon>): String = JSONArray(beacons.map { it.toJson() }).toString()

    @ColumnTypeConverter
    fun toBeacons(str: String): List<PassBeacon> = JSONArray(str).map { PassBeacon.fromJson(it) }

    @ColumnTypeConverter
    fun fromLinks(links: Map<String, String>): String = JSONObject(links).toString()

    @ColumnTypeConverter
    fun toLinks(str: String): Map<String, String> =
        JSONObject(str).let { json -> json.keys().asSequence().associateWith { json.getString(it) } }
}

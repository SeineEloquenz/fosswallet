package nz.eloque.foss_wallet.persistence

import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassRelevantDate
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZonedDateTime
import java.util.UUID

class TypeConverters {

    @TypeConverter
    fun fromZonedDateTime(dateTime: ZonedDateTime): String {
        return dateTime.toString()
    }

    @TypeConverter
    fun toZonedDateTime(dateTime: String) : ZonedDateTime {
        return ZonedDateTime.parse(dateTime)
    }

    @TypeConverter
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

    @TypeConverter
    fun toRelevantDates(str: String): List<PassRelevantDate> {
        return JSONArray(str).map {
            if (it.has("date"))
                PassRelevantDate.Date(
                    ZonedDateTime.parse(it.getString("date"))
                )
            else
                PassRelevantDate.DateInterval(
                    ZonedDateTime.parse(it.getString("startDate")),
                    ZonedDateTime.parse(it.getString("endDate"))
                )
        }
    }

    @TypeConverter
    fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(instant: Long) : Instant {
        return Instant.ofEpochMilli(instant)
    }

    @TypeConverter
    fun fromColors(colors: PassColors): String {
        return "${colors.background.toArgb()},${colors.foreground.toArgb()},${colors.label.toArgb()}"
    }

    @TypeConverter
    fun toColors(colors: String): PassColors {
        val split = colors.split(",")
        return PassColors(Color(split[0].toInt()), Color(split[1].toInt()), Color(split[2].toInt()))
    }

    @TypeConverter
    fun fromColor(color: Color): String = color.toArgb().toString()

    @TypeConverter
    fun toColor(color: String): Color = Color(color.toInt())

    @TypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun toUuid(uuid: String): UUID = UUID.fromString(uuid)

    @TypeConverter
    fun fromPassType(passType: PassType): String {
        return when (passType) {
            is PassType.Boarding -> passType.jsonKey + "," + passType.transitType.toString()
            is PassType.Coupon -> passType.jsonKey
            is PassType.Event -> passType.jsonKey
            is PassType.Generic -> passType.jsonKey
            is PassType.StoreCard -> passType.jsonKey
        }
    }

    @TypeConverter
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

    @TypeConverter
    fun fromLocations(locations: List<Location>): String {
        val json = JSONArray()
        locations.forEach {
            val locJson = JSONObject()
            locJson.put("latitude", it.latitude)
            locJson.put("longitude", it.longitude)
            json.put(locJson)
        }
        return json.toString()
    }

    @TypeConverter
    fun toLocations(str: String): List<Location> {
        return JSONArray(str).map {
            val location = Location("")
            location.latitude = it.getDouble("latitude")
            location.longitude = it.getDouble("longitude")
            location
        }
    }

    @TypeConverter
    fun fromBarcodes(barcodes: Set<BarCode>): String {
        val json = JSONArray()
        barcodes.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @TypeConverter
    fun toBarcodes(str: String): Set<BarCode> {
        return JSONArray(str).map { BarCode.fromJson(it) }.toSet()
    }

    @TypeConverter
    fun fromFields(fields: List<PassField>): String {
        val json = JSONArray()
        fields.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @TypeConverter
    fun toFields(str: String): List<PassField> {
        return JSONArray(str).map { PassField.fromJson(it) }
    }
}

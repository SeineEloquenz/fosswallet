package nz.eloque.foss_wallet.persistence

import android.location.Location
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.utils.forEach
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.LinkedList
import java.util.UUID

class TypeConverters {

    @TypeConverter
    fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(instant: Long) : Instant {
        return Instant.ofEpochMilli(instant)
    }

    @TypeConverter
    fun fromColor(colors: PassColors): String {
        return "${colors.background.toArgb()},${colors.foreground.toArgb()},${colors.label.toArgb()}"
    }

    @TypeConverter
    fun toColor(colors: String): PassColors {
        val split = colors.split(",")
        return PassColors(Color(split[0].toInt()), Color(split[1].toInt()), Color(split[2].toInt()))
    }

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
                PassType.EVENT -> PassType.Event()
                PassType.COUPON -> PassType.Coupon()
                PassType.STORE_CARD -> PassType.StoreCard()
                else -> PassType.Generic()
            }
        }
    }

    @TypeConverter
    fun fromLocations(locations: MutableList<Location>): String {
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
    fun toLocations(str: String): MutableList<Location> {
        val json = JSONArray(str)
        val locations = LinkedList<Location>()
        json.forEach {
            val location = Location("")
            location.latitude = it.getDouble("latitude")
            location.longitude = it.getDouble("longitude")
            locations.add(location)
        }
        return locations
    }

    @TypeConverter
    fun fromBarcodes(barcodes: Set<BarCode>): String {
        val json = JSONArray()
        barcodes.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @TypeConverter
    fun toBarcodes(str: String): Set<BarCode> {
        val json = JSONArray(str)
        val barcodes = HashSet<BarCode>()
        json.forEach { barcodes.add(BarCode.fromJson(it)) }
        return barcodes
    }

    @TypeConverter
    fun fromFields(fields: MutableList<PassField>): String {
        val json = JSONArray()
        fields.forEach { json.put(it.toJson()) }
        return json.toString()
    }

    @TypeConverter
    fun toFields(str: String): MutableList<PassField> {
        val json = JSONArray(str)
        val fields = LinkedList<PassField>()
        json.forEach { fields.add(PassField.fromJson(it)) }
        return fields
    }
}

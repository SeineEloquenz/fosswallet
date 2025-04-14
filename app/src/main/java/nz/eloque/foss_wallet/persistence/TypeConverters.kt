package nz.eloque.foss_wallet.persistence

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.room.TypeConverter
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.utils.forEach
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.LinkedList

class TypeConverters {

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val os = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        return os.toByteArray()
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

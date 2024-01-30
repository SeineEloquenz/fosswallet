package nz.eloque.foss_wallet.model

import android.graphics.Bitmap
import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.util.LinkedList


@Entity
data class PassField(val key: String, val label: String, val value: String) {
    fun toJson(): JSONObject {
        return JSONObject().also {
            it.put("key", key)
            it.put("label", label)
            it.put("value", value)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): PassField {
            return PassField(json.getString("key"), json.getString("label"), json.getString("value"))
        }
    }
}

@Entity
data class Pass(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val icon: Bitmap,
    val barCodes: Set<BarCode>
) {
    var organization: String? = null
    var type: PassType = PassType.EVENT
    var serialNumber: String? = null
    var logo: Bitmap? = null
    var strip: Bitmap? = null
    var footer: Bitmap? = null
    var locations: MutableList<Location> = LinkedList()
    var headerFields: MutableList<PassField> = LinkedList()
    var primaryFields: MutableList<PassField> = LinkedList()
    var secondaryFields: MutableList<PassField> = LinkedList()
    var auxiliaryFields: MutableList<PassField> = LinkedList()
    var backFields: MutableList<PassField> = LinkedList()

    companion object {
        fun placeholder(): Pass {
            return Pass(0, "Loading", Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888), setOf())
        }
    }
}
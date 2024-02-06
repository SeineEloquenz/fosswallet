package nz.eloque.foss_wallet.model

import android.content.Context
import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject
import java.io.File
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
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val description: String,
    val barCodes: Set<BarCode>,
    val hasLogo: Boolean = false,
    val hasStrip: Boolean = false,
    val hasThumbnail: Boolean = false,
    val hasFooter: Boolean = false,
) {
    var relevantDate: Long = 0
    var expirationDate: Long = 0
    var organization: String? = null
    var type: PassType = PassType.EVENT
    var serialNumber: String? = null
    var authToken: String? = null
    var webServiceUrl: String? = null
    var passIdent: String? = null
    var locations: MutableList<Location> = LinkedList()
    var headerFields: MutableList<PassField> = LinkedList()
    var primaryFields: MutableList<PassField> = LinkedList()
    var secondaryFields: MutableList<PassField> = LinkedList()
    var auxiliaryFields: MutableList<PassField> = LinkedList()
    var backFields: MutableList<PassField> = LinkedList()

    fun iconFile(context: Context): File = coilImageModel(context, "icon", true)!!
    fun logoFile(context: Context): File? = coilImageModel(context, "logo", hasLogo)
    fun stripFile(context: Context): File? = coilImageModel(context, "strip", hasStrip)
    fun thumbnailFile(context: Context): File? = coilImageModel(context, "thumbnail", hasThumbnail)
    fun footerFile(context: Context): File? = coilImageModel(context, "footer", hasFooter)

    private fun coilImageModel(context: Context, type: String, exists: Boolean): File? {
        return if (exists) File(context.filesDir, "$id/$type.png") else null
    }

    fun deleteFiles(context: Context) {
        iconFile(context).delete()
        logoFile(context)?.delete()
        stripFile(context)?.delete()
        thumbnailFile(context)?.delete()
        footerFile(context)?.delete()
        File(context.filesDir, "$id").delete()
    }

    companion object {
        fun placeholder(): Pass {
            return Pass(0, "Loading", setOf())
        }
    }
}
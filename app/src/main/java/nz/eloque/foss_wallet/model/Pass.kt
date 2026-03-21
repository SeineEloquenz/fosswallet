package nz.eloque.foss_wallet.model

import android.content.Context
import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.inIgnoreCase
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime
import java.util.LinkedList
import java.util.UUID

@Entity(
    tableName = "Pass",
    foreignKeys = [
        ForeignKey(
            entity = PassGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class Pass(
    @PrimaryKey val id: String,
    val description: String,
    val formatVersion: Int,
    val organization: String,
    val serialNumber: String,
    val type: PassType,
    val barCodes: Set<BarCode>,
    @ColumnInfo(defaultValue = "0")
    val addedAt: Instant,
    val hasLogo: Boolean = false,
    val hasStrip: Boolean = false,
    val hasThumbnail: Boolean = false,
    val hasFooter: Boolean = false,
    /**
     * Device UUID used for updating passes.
     * We use a unique UUID per pass so devices can not be linked across servers from the UUID
     */
    @ColumnInfo(defaultValue = "2b767e5b-75fd-4bec-89d7-188e832b2dc3")
    val deviceId: UUID = UUID.randomUUID(),
    val colors: PassColors? = null,
    val groupId: Long? = null,
    val relevantDates: List<PassRelevantDate> = LinkedList(),
    val expirationDate: ZonedDateTime? = null,
    val logoText: String? = null,
    val authToken: String? = null,
    val webServiceUrl: String? = null,
    val passTypeIdentifier: String? = null,
    val locations: List<Location> = LinkedList(),
    val headerFields: List<PassField> = LinkedList(),
    val primaryFields: List<PassField> = LinkedList(),
    val secondaryFields: List<PassField> = LinkedList(),
    val auxiliaryFields: List<PassField> = LinkedList(),
    val backFields: List<PassField> = LinkedList(),
    @ColumnInfo(defaultValue = "0")
    val archived: Boolean = false,
    @ColumnInfo(defaultValue = "1")
    val autoArchive: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val renderLegacy: Boolean = false,
) {
    fun iconFile(context: Context): File = coilImageModel(context, "icon", true)!!
    fun logoFile(context: Context): File? = coilImageModel(context, "logo", hasLogo)
    fun stripFile(context: Context): File? = coilImageModel(context, "strip", hasStrip)
    fun thumbnailFile(context: Context): File? = coilImageModel(context, "thumbnail", hasThumbnail)
    fun footerFile(context: Context): File? = coilImageModel(context, "footer", hasFooter)

    fun contains(query: String): Boolean {
        return when {
            query inIgnoreCase description -> true
            query inIgnoreCase type.jsonKey -> true
            query inIgnoreCase logoText -> true
            headerFields.any { it.contains(query) } -> true
            primaryFields.any { it.contains(query) } -> true
            secondaryFields.any { it.contains(query) } -> true
            auxiliaryFields.any { it.contains(query) } -> true
            backFields.any { it.contains(query) } -> true
            else -> false
        }
    }

    fun allFields(): List<PassField> = headerFields + primaryFields + secondaryFields + auxiliaryFields + backFields

    fun updatedFields(oldPass: Pass): List<PassField> {
        val myFields = this.allFields()
        val updatedFields = oldPass.allFields().associateBy { it.key }

        return myFields
            .filter { it.hasChangeMessage() }
            .filter { it.content != updatedFields[it.key]?.content }
    }

    fun updatable(): Boolean {
        return webServiceUrl != null
                && authToken != null
                && passTypeIdentifier != null
    }

    private fun coilImageModel(context: Context, type: String, exists: Boolean): File? {
        return if (exists) File(context.filesDir, "$id/$type.png") else null
    }

    fun originalPassFile(context: Context): File? {
        val file = File(context.filesDir, "$id/${OriginalPass.FILE_PATH}")
        return if (file.exists()) file else null
    }

    fun deleteFiles(context: Context) {
        iconFile(context).delete()
        logoFile(context)?.delete()
        stripFile(context)?.delete()
        thumbnailFile(context)?.delete()
        footerFile(context)?.delete()
        File(context.filesDir, id).delete()
    }

    companion object {
        fun placeholder(): Pass {
            return Pass("", "Loading", 1, "", "", PassType.Generic, setOf(), addedAt = Instant.ofEpochMilli(0))
        }
    }

    fun MutableList<PassField>.contains(query: String): Boolean {
        return this.any { query in (it.label ?: "") || it.content.contains(query) }
    }
}

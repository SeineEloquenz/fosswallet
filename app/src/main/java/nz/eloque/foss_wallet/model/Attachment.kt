package nz.eloque.foss_wallet.model

import android.content.Context
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.io.File
import java.io.FileOutputStream

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Pass::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("passId"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["passId"]),
    ],
)
data class Attachment(
    @PrimaryKey
    val fileName: String,
    val passId: String,
) {
    fun delete(context: Context) {
        val file = File(context.filesDir, "$passId/attachments/$fileName")
        file.delete()
    }

    fun getFile(context: Context): File = File(context.filesDir, "$passId/attachments/$fileName")

    fun save(
        context: Context,
        bytes: ByteArray,
    ) {
        val file =
            File(context.filesDir, "$passId/attachments/$fileName").apply {
                parentFile?.mkdirs()
                createNewFile()
            }
        FileOutputStream(file).use {
            it.write(bytes)
        }
    }
}

package nz.eloque.foss_wallet.model

import android.content.Context
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
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
)
data class Attachment(
    @PrimaryKey
    val fileName: String,
    val passId: String,
) {
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

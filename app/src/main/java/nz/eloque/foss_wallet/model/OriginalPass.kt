package nz.eloque.foss_wallet.model

import android.content.Context
import java.io.File
import java.io.FileOutputStream

data class OriginalPass(
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OriginalPass

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    fun saveToDisk(context: Context, passId: Long) {
        val directory = File(context.filesDir, "$passId")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        FileOutputStream(File(directory, FILE_PATH)).use {
            it.write(bytes)
        }
    }

    companion object {
        const val FILE_PATH = "original.pkpass"
    }
}
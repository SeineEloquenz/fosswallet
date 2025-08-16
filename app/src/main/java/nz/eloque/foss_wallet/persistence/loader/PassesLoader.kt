package nz.eloque.foss_wallet.persistence.loader

import nz.eloque.foss_wallet.utils.toByteArray
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class PassesLoader(val passLoader: PassLoader) {

    fun load(bytes: ByteArray): Set<PassLoadResult> {
        return try {
            val results = mutableSetOf<PassLoadResult>()

            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.endsWith(".pkpass", ignoreCase = true)) {
                        val passBytes = zip.toByteArray()
                        val result = passLoader.load(passBytes)
                        results.add(result)
                    }
                    entry = zip.nextEntry
                }
            }

            results
        } catch (e: Exception) {
            throw InvalidPassesException(e)
        }
    }
}
package nz.eloque.foss_wallet.persistence.loader

import android.content.Context
import nz.eloque.foss_wallet.parsing.PassParser
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

enum class Input {
    PKPASS,
    PKPASSES,
}

sealed class InvalidInputException : Exception {
    constructor() : super()
    constructor(e: Exception) : super(e)
}

class InvalidPassException : InvalidInputException {
    constructor() : super()
    constructor(e: Exception) : super(e)
}

class InvalidPassesException : InvalidInputException {
    constructor() : super()
    constructor(e: Exception) : super(e)
}

class UnknownInputException : InvalidInputException {
    constructor() : super()
    constructor(e: Exception) : super(e)
}

class Loader(
    val context: Context,
) {
    /** Parses the passes of a pkpass or pkpasses file. */
    @Throws(InvalidInputException::class)
    fun load(bytes: ByteArray): Set<PassLoadResult> {
        val passParser = PassParser(context)

        val type = detectFileType(bytes)
        return when (type) {
            Input.PKPASS -> setOf(PassLoader(passParser).load(bytes))
            Input.PKPASSES -> PassesLoader(PassLoader(passParser)).load(bytes)
        }
    }

    private fun detectFileType(bytes: ByteArray): Input {
        val zipStream = ZipInputStream(ByteArrayInputStream(bytes))
        val entries = mutableListOf<String>()
        var entry = zipStream.nextEntry
        while (entry != null) {
            entries.add(entry.name)
            entry = zipStream.nextEntry
        }
        zipStream.close()
        return when {
            entries.contains("pass.json") -> Input.PKPASS
            entries.isNotEmpty() && entries.all { it.endsWith(".pkpass") } -> Input.PKPASSES
            else -> throw UnknownInputException()
        }
    }
}

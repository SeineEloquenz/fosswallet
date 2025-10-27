package nz.eloque.foss_wallet.persistence.loader

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.ui.screens.wallet.PassViewModel
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

private const val TAG: String = "Loader"

enum class Input {
    PKPASS,
    PKPASSES,
}

sealed class LoaderResult {
    data class Single(val passId: String) : LoaderResult()
    object Multiple : LoaderResult()
    object Invalid : LoaderResult()
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

class Loader(val context: Context) {
    
    fun handleInputStream(
        inputStream: InputStream,
        passViewModel: PassViewModel,
        coroutineScope: CoroutineScope,
    ): LoaderResult {
        val loadResults = try {
            this.load(inputStream)
        } catch (e: InvalidInputException) {
            Log.e(TAG, "Failed to load pass from intent: $e")
            coroutineScope.launch(Dispatchers.Main) { Toast
                .makeText(context, context.getString(R.string.invalid_pass_toast), Toast.LENGTH_SHORT)
                .show() }
            return LoaderResult.Invalid
        }
        if (loadResults.size == 1) {
            val loadResult = loadResults.first()
            val importResult = passViewModel.add(loadResult)
            val id: String = loadResult.pass.pass.id
            coroutineScope.launch(Dispatchers.Main) {
                when (importResult) {
                    is ImportResult.Replaced -> {
                        Toast
                            .makeText(context, context.getString(R.string.pass_already_imported), Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        Toast
                            .makeText(context, context.getString(R.string.pass_imported), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            return LoaderResult.Single(id)
        } else {
            loadResults.forEach { result -> passViewModel.add(result) }
            coroutineScope.launch(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.n_passes_imported, loadResults.size), Toast.LENGTH_SHORT)
                    .show()
            }
            return LoaderResult.Multiple
        }
    }
    
    @Throws(InvalidInputException::class)
    private fun load(input: InputStream): Set<PassLoadResult> {
        val passParser = PassParser(context)
        val bytes = input.readBytes()

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
            entries.all { it.endsWith(".pkpass") } -> Input.PKPASSES
            else -> throw UnknownInputException()
        }
    }
}

package nz.eloque.foss_wallet.persistence.loader

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.api.ImportResult
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.parsing.PassParser
import nz.eloque.foss_wallet.persistence.bundle.TagsSerializer
import nz.eloque.foss_wallet.ui.screens.wallet.WalletViewModel
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

private const val TAG: String = "Loader"

enum class Input {
    PKPASS,
    PKPASSES,
}

sealed class LoaderResult {
    data class Single(
        val passId: String,
    ) : LoaderResult()

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

class Loader(
    val context: Context,
) {
    suspend fun handleInputStream(
        inputStream: InputStream,
        walletViewModel: WalletViewModel,
        coroutineScope: CoroutineScope,
    ): LoaderResult {
        val bundle =
            try {
                this.load(inputStream)
            } catch (e: InvalidInputException) {
                Log.e(TAG, "Failed to load pass from intent: $e")
                coroutineScope.launch(Dispatchers.Main) {
                    Toast
                        .makeText(context, context.getString(R.string.invalid_pass_toast), Toast.LENGTH_SHORT)
                        .show()
                }
                return LoaderResult.Invalid
            }
        val loadResults = bundle.passes
        if (loadResults.isEmpty()) {
            coroutineScope.launch(Dispatchers.Main) {
                Toast
                    .makeText(context, context.getString(R.string.no_passes_found_in_file), Toast.LENGTH_SHORT)
                    .show()
            }
            return LoaderResult.Invalid
        }
        if (loadResults.size == 1) {
            val loadResult = loadResults.first()
            val importResult = walletViewModel.add(loadResult)
            applyTags(loadResult, bundle.tagsByPass, walletViewModel)
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
            loadResults.forEach { result ->
                walletViewModel.add(result)
                applyTags(result, bundle.tagsByPass, walletViewModel)
            }

            walletViewModel.group(loadResults.map { it.pass.pass }.toSet())

            coroutineScope.launch(Dispatchers.Main) {
                Toast
                    .makeText(context, context.getString(R.string.n_passes_imported, loadResults.size), Toast.LENGTH_SHORT)
                    .show()
            }
            return LoaderResult.Multiple
        }
    }

    private suspend fun applyTags(
        loadResult: PassLoadResult,
        tagsByPass: Map<String, Set<Tag>>,
        walletViewModel: WalletViewModel,
    ) {
        tagsByPass[loadResult.pass.pass.id]?.forEach { tag ->
            walletViewModel.importTag(loadResult.pass.pass, tag)
        }
    }

    @Throws(InvalidInputException::class)
    private fun load(input: InputStream): LoadedBundle {
        val passParser = PassParser(context)
        val bytes = input.readBytes()

        val type = detectFileType(bytes)
        return when (type) {
            Input.PKPASS -> LoadedBundle(setOf(PassLoader(passParser).load(bytes)), emptyMap())
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
            entries.any { it.endsWith(".pkpass") } &&
                entries.all { it.endsWith(".pkpass") || it == TagsSerializer.BUNDLE_ENTRY } -> Input.PKPASSES
            else -> throw UnknownInputException()
        }
    }
}

package nz.eloque.foss_wallet.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.bundle.PassBundler
import java.io.File
import java.io.IOException

sealed class BundleShareResult {
    data class Shared(
        val shared: Int,
        val skipped: Int,
    ) : BundleShareResult()

    object NothingToShare : BundleShareResult()
}

fun share(
    passes: Collection<Pass>,
    context: Context,
    tagsByPass: Map<String, Set<Tag>> = emptyMap(),
): BundleShareResult {
    val exportable = passes.filter { it.originalPassFile(context) != null }
    if (exportable.isEmpty()) {
        return BundleShareResult.NothingToShare
    }
    val bundler = PassBundler(context)
    val passesFile = bundler.bundle(exportable, tagsByPass)
    val uri = uri(context, passesFile)
    shareFile(uri, "application/vnd.apple.pkpasses", context)
    return BundleShareResult.Shared(exportable.size, passes.size - exportable.size)
}

fun save(
    passes: Collection<Pass>,
    target: Uri,
    context: Context,
    tagsByPass: Map<String, Set<Tag>> = emptyMap(),
): BundleShareResult {
    val exportable = passes.filter { it.originalPassFile(context) != null }
    if (exportable.isEmpty()) {
        return BundleShareResult.NothingToShare
    }
    val bundle = PassBundler(context).bundle(exportable, tagsByPass)
    val output = context.contentResolver.openOutputStream(target) ?: throw IOException("Could not open $target for writing")
    output.use { out ->
        bundle.inputStream().use { it.copyTo(out) }
    }
    return BundleShareResult.Shared(exportable.size, passes.size - exportable.size)
}

fun share(
    passFile: File,
    context: Context,
) {
    val uri = uri(context, passFile)
    shareFile(uri, "application/vnd.apple.pkpass", context)
}

private fun shareFile(
    uri: Uri,
    type: String,
    context: Context,
) {
    val sendIntent =
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            setDataAndType(uri, type)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    val shareIntent = Intent.createChooser(sendIntent, "Share pass")
    context.startActivity(shareIntent)
}

private fun uri(
    context: Context,
    file: File,
): Uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)

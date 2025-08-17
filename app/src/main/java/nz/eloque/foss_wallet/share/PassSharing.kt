package nz.eloque.foss_wallet.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.bundle.PassBundler
import java.io.File

fun share(
    passes: Collection<Pass>,
    context: Context,
) {
    val bundler = PassBundler(context)
    val passesFile = bundler.bundle(passes)
    val uri = uri(context, passesFile)
    shareFile(uri, "application/vnd.apple.pkpasses", context)
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
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        setDataAndType(uri, type)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share pass")
    context.startActivity(shareIntent)
}

private fun uri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
}
package nz.eloque.foss_wallet.ui.screens.pass

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import java.io.File

@Composable
fun PassShareButton(
    file: File
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    DropdownMenuItem(
        text = { Text(stringResource(R.string.share)) },
        leadingIcon =  {
            Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share))
        },
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                val uri = uri(context, file)
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                    setDataAndType(uri, "application/vnd.apple.pkpass")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share pass")
                context.startActivity(shareIntent)
            }
        }
    )
}

private fun uri(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
}
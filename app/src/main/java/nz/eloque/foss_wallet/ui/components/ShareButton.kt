package nz.eloque.foss_wallet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.share.share
import java.io.File

@Composable
fun ShareButton(file: File) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                share(file, context)
            }
        },
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = stringResource(R.string.share),
        )
    }
}

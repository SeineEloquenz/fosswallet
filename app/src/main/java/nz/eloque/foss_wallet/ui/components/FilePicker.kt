package nz.eloque.foss_wallet.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R

@Composable
fun FilePicker(
    label: String,
    labelIcon: ImageVector,
    onChoose: (String?, Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            println("selected file URI $uri")
            uri?.let { onChoose(getFileName(context, uri), uri) }
        }
    val launchPicker = {
        launcher.launch(
            arrayOf(
                "image/png",
                "image/jpeg",
                "application/pdf",
            ),
        )
    }

    TextButton(
        onClick = {
            launchPicker()
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label)
            Icon(
                imageVector = labelIcon,
                contentDescription = stringResource(R.string.choose_image),
            )
        }
    }
}

fun getFileName(
    context: Context,
    uri: Uri,
): String? {
    var name: String? = null

    val cursor =
        context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null,
        )

    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                name = it.getString(index)
            }
        }
    }

    return name
}

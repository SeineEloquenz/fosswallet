package nz.eloque.foss_wallet.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R

@Composable
fun ImagePicker(
    imageUrl: Uri?,
    onClear: () -> Unit,
    onChoose: (Uri?) -> Unit,
    label: String? = null,
    labelIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        println("selected file URI $uri")
        onChoose(uri)
    }
    val launchPicker = {
        launcher.launch(arrayOf(
            "image/png",
            "image/jpeg",
        ))
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { launchPicker() },
    ) {
        if (labelIcon != null) {
            Icon(
                imageVector = labelIcon,
                contentDescription = label ?: stringResource(R.string.image),
            )
        }

        if (label != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }

        Spacer(modifier = Modifier.weight(1f))

        AsyncImage(
            model = imageUrl ?: R.drawable.icon,
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(40.dp)
        )

        IconButton(
            onClick = {
                onClear()
            },
            enabled = imageUrl != null
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.clear_selection)
            )
        }

        IconButton(
            onClick = {
                launchPicker()
            }
        ) {
            Icon(
                imageVector = Icons.Default.ImageSearch,
                contentDescription = stringResource(R.string.choose_image)
            )
        }
    }
}

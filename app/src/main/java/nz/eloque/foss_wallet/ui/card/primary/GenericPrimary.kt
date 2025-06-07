package nz.eloque.foss_wallet.ui.card.primary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.card.MainLabel


@Composable
fun GenericPrimary(pass: Pass) {
    val context = LocalContext.current

    Row {
        val thumbnailFile = pass.thumbnailFile(context)
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            modifier = Modifier.fillMaxWidth(if (thumbnailFile != null) {0.6f} else {1.0f})
        ) {
            pass.primaryFields.firstOrNull()?.let {
                MainLabel(it.label, it.content)
            }
        }
        AsyncImage(
            model = thumbnailFile,
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(5.dp)
                .width(120.dp)
                .height(150.dp)
        )
    }
}
package nz.eloque.foss_wallet.ui.card.primary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.field.isNotEmpty
import nz.eloque.foss_wallet.ui.card.AutoSizePassFields
import nz.eloque.foss_wallet.ui.card.PassField

@Composable
fun GenericPrimary(
    pass: Pass,
    isSelectable: Boolean = true
) {
    val context = LocalContext.current

    val primaryField = pass.primaryFields.firstOrNull()
    val thumbnailFile = pass.thumbnailFile(context)

    if (primaryField.isNotEmpty() || thumbnailFile != null) {
        Row(
            modifier = Modifier.height(90.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            primaryField?.let {
                AutoSizePassFields(
                    fields = listOf(it),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    useFixedWidth = true
                ) { fontSize ->
                    PassField(
                        field = it,
                        fontSize = fontSize,
                        maxLines = 2,
                        isSelectable = isSelectable
                    )
                }
            } ?: Spacer(Modifier.weight(1f))

            thumbnailFile?.let {
                AsyncImage(
                    model = it,
                    contentDescription = stringResource(R.string.image),
                    modifier = Modifier.widthIn(max = 120.dp).fillMaxHeight()
                )
            }
        }
    }
}

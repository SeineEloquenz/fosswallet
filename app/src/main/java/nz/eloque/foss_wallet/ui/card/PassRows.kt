package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.ui.components.FairRow

@Composable
fun HeaderRow(
    pass: Pass,
    isSelectable: Boolean = true,
) {
    val context = LocalContext.current

    BoxWithConstraints {
        Row(
            modifier = Modifier.height(38.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            pass.logoFile(context)?.let {
                AsyncImage(
                    model = it,
                    contentDescription = stringResource(R.string.image),
                    modifier = Modifier.widthIn(max = this@BoxWithConstraints.maxWidth * 0.4f),
                )
            }

            pass.logoText?.let {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                )
            } ?: Spacer(Modifier.weight(1f))

            FieldsRow(
                fields = pass.headerFields.reversed(),
                modifier = Modifier.widthIn(max = this@BoxWithConstraints.maxWidth * 0.5f),
                arrangeWithSpaceBetween = false,
                horizontalAlignment = Alignment.End,
                isSelectable = isSelectable,
            )
        }
    }
}

@Composable
fun FieldsRow(
    fields: List<PassField>,
    modifier: Modifier = Modifier,
    arrangeWithSpaceBetween: Boolean = true,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    isSelectable: Boolean = true,
) {
    val space = 10.dp

    if (fields.isNotEmpty()) {
        AutoSizePassFields(
            fields = fields,
            modifier = modifier.height(38.dp),
            spacing = space,
        ) { fontSize ->
            FairRow(
                spacing = space,
                arrangeWithSpaceBetween = arrangeWithSpaceBetween,
            ) {
                fields.forEach {
                    PassField(
                        field = it,
                        horizontalAlignment = horizontalAlignment,
                        fontSize = fontSize,
                        isSelectable = isSelectable,
                    )
                }
            }
        }
    }
}

package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import nz.eloque.compose_kit.layout.FairRow
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.field.PassField
import java.io.File

@Composable
fun HeaderRow(
    pass: Pass,
    logo: File?,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = true,
) {
    BoxWithConstraints(modifier = modifier) {
        Row(
            modifier = Modifier.height(38.dp),
            horizontalArrangement = Arrangement.spacedBy(PassCardDefaults.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            logo?.let {
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

            if (pass.voided) VoidedChip()

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
    if (fields.isNotEmpty()) {
        AutoSizePassFields(
            fields = fields,
            modifier = modifier.height(38.dp),
            spacing = PassCardDefaults.spacing,
        ) { fontSize ->
            FairRow(
                spacing = PassCardDefaults.spacing,
                arrangeWithSpaceBetween = arrangeWithSpaceBetween,
            ) {
                fields.forEach {
                    PassField(
                        field = it,
                        horizontalAlignment = it.textAlignment?.horizontalAlignment() ?: horizontalAlignment,
                        fontSize = fontSize,
                        isSelectable = isSelectable,
                    )
                }
            }
        }
    }
}

@Composable
private fun VoidedChip() {
    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, LocalContentColor.current),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = stringResource(R.string.pass_voided), style = MaterialTheme.typography.labelLarge)
        }
    }
}

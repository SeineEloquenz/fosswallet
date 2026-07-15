package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import java.io.File

@Composable
fun ThumbnailPrimary(
    primaryFields: List<PassField>,
    thumbnail: File?,
    modifier: Modifier = Modifier,
    secondaryFields: List<PassField>? = null,
    isSelectable: Boolean = true,
) {
    val primaryField = primaryFields.getOrElse(0) { PassField.Empty }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PassCardDefaults.spacing),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PassCardDefaults.spacing),
        ) {
            val maxLines = if (secondaryFields == null) 2 else 1
            AutoSizePassFields(
                fields = listOf(primaryField),
                modifier = Modifier.weight(1f),
                maxLines = maxLines,
                useFixedWidth = true,
            ) { fontSize ->
                PassField(
                    field = primaryField,
                    fontSize = fontSize,
                    maxLines = maxLines,
                    isSelectable = isSelectable,
                )
            }

            secondaryFields?.let {
                FieldsRow(
                    fields = it,
                    isSelectable = isSelectable,
                )
            }
        }

        thumbnail?.let {
            AsyncImage(
                model = it,
                contentDescription = stringResource(R.string.image),
                modifier = Modifier.widthIn(max = 120.dp).fillMaxHeight(),
            )
        }
    }
}

@Composable
fun StripImagePrimary(
    primaryFields: List<PassField>,
    stripImage: File?,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = true,
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = stripImage,
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
        )

        primaryFields.firstOrNull()?.let {
            AutoSizePassFields(
                fields = listOf(it),
                modifier = Modifier.padding(PassCardDefaults.padding).fillMaxHeight(0.75f),
                useFixedWidth = true,
                labelStyle = PassCardDefaults.labelStyleStripImage,
            ) { fontSize ->
                StripImagePrimaryField(
                    field = it,
                    fontSize = fontSize,
                    isSelectable = isSelectable,
                )
            }
        }
    }
}

@Composable
fun BoardingPrimary(
    primaryFields: List<PassField>,
    transitType: TransitType,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = true,
) {
    val departureField = primaryFields.getOrElse(0) { PassField.Empty }
    val destinationField = primaryFields.getOrElse(1) { PassField.Empty }

    val iconWidth = 40.dp
    val space = PassCardDefaults.spacing

    AutoSizePassFields(
        fields = listOf(departureField, destinationField),
        modifier = modifier,
        spacing = iconWidth + space * 2,
        useFixedWidth = true,
    ) { fontSize ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(space),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PassField(
                field = departureField,
                modifier = Modifier.weight(1f),
                fontSize = fontSize,
                isSelectable = isSelectable,
            )

            Column {
                // The text correctly spaces the icon
                Text("", style = PassCardDefaults.labelStyle)
                Icon(
                    imageVector = transitType.icon,
                    contentDescription = stringResource(R.string.to),
                    modifier = Modifier.width(iconWidth).fillMaxHeight(),
                )
            }

            PassField(
                field = destinationField,
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                fontSize = fontSize,
                isSelectable = isSelectable,
            )
        }
    }
}

package nz.eloque.foss_wallet.ui.components.card_contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.ui.components.AbbreviatingText
import nz.eloque.foss_wallet.ui.components.DTPassLabel


@Composable
fun DeutschlandTicketContent(
    pass: Pass,
    cardColors: CardColors,
    modifier: Modifier = Modifier,
    content: @Composable ((CardColors) -> Unit)
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row {
            AsyncImage(
                model = pass.logoFile(context),
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.padding(5.dp)
                    .fillMaxWidth(0.7f)
            )
            pass.headerFields.firstOrNull()?.let { DTPassLabel(
                label = it.label,
                content = it.content,
                labelTextAlign = TextAlign.Right,
            ) }
        }
        Row {
            pass.primaryFields.firstOrNull()?.let { DTMainLabel(it.label, it.content, Modifier.padding(5.dp)
                    .fillMaxWidth(0.6f)) }
            AsyncImage(
                model = pass.thumbnailFile(context) ?: pass.iconFile(context),
                contentDescription = stringResource(R.string.image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(5.dp)
                    .width(120.dp)
                    .height(150.dp)
            )
        }
        DTSecondaryFields(pass.secondaryFields)
        DTAuxiliaryFields(pass.auxiliaryFields)
        content.invoke(cardColors)
    }
}

@Composable
private fun DTMainLabel(
    label: String,
    content: PassContent,
    modifier: Modifier = Modifier
) {
    Box(
        modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            AbbreviatingText(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall,
            )
            SelectionContainer {
                val contentString = content.prettyPrint()
                Text(
                    text = contentString,
                    minLines = 2,
                    maxLines = 2,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}
@Composable
private fun DTSecondaryFields(
    fields: List<PassField>
) {
    Row(
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        fields.dropLast(1).forEach { DTPassLabel(it.label, it.content) }
        fields.lastOrNull()?.let { DTPassLabel(it.label, it.content, Modifier, TextAlign.Right) }
    }
}
@Composable
private fun DTAuxiliaryFields(
    fields: List<PassField>
) {
    Row(
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        fields.firstOrNull()?.let { DTPassLabel(it.label, it.content) }
        fields.lastOrNull()?.let { DTPassLabel(it.label, it.content, Modifier, TextAlign.Right) }
    }
}

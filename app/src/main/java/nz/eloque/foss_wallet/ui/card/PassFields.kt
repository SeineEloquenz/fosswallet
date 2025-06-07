package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.ui.components.AbbreviatingText
import nz.eloque.foss_wallet.ui.components.DateView
import nz.eloque.foss_wallet.ui.components.LocationButton
import nz.eloque.foss_wallet.ui.view.pass.HeaderFieldsView

@Composable
fun HeaderRow(
    pass: Pass
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
            .padding(12.dp)
    ) {
        AsyncImage(
            model = pass.logoFile(context),
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.padding(5.dp)
                .height(28.dp)
        )
        pass.logoText?.let {
            AbbreviatingText(
                text = pass.logoText!!,
                maxLines = 1,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }

        HeaderFieldsView(
            headerFields = pass.headerFields
        )
    }
}

@Composable
fun DateLocationRow(
    pass: Pass
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateView(pass.description, pass.relevantDate, pass.expirationDate)
        pass.locations.firstOrNull()?.let { LocationButton(it) }
    }
}

@Composable
fun SecondaryFields(
    fields: List<PassField>
) {
    if (fields.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            fields.dropLast(1).forEach { PlainPassLabel(it.label, it.content) }
            fields.lastOrNull()?.let { PlainPassLabel(it.label, it.content, Modifier, LabelAlign.RIGHT) }
        }
    }
}

@Composable
fun AuxiliaryFields(
    fields: List<PassField>
) {
    if (fields.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            fields.firstOrNull()?.let { PlainPassLabel(it.label, it.content) }
            fields.lastOrNull()?.let { PlainPassLabel(it.label, it.content, Modifier, LabelAlign.RIGHT) }
        }
    }
}

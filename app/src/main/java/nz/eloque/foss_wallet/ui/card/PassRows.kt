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
import nz.eloque.foss_wallet.ui.screens.pass.HeaderFieldsView
import java.io.File

@Composable
fun HeaderRow(
    pass: Pass
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
            .padding(12.dp)
    ) {
        LogoView(
            pass.logoFile(context),
            pass.logoText,
            Modifier.weight(1f)
        )

        HeaderFieldsView(
            headerFields = pass.headerFields
        )
    }
}

@Composable
private fun LogoView(
    logoFile: File?,
    logoText: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
    ) {
        logoFile?.let { AsyncImage(
            model = it,
            contentDescription = stringResource(R.string.image),
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.padding(5.dp)
                .height(28.dp)
        ) }
        logoText?.let {
            AbbreviatingText(
                text = it,
                maxLines = 1,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
        }
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
fun FieldsRow(
    fields: List<PassField>
) {
    if (fields.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (fields.size) {
                1 -> PlainPassLabel(fields[0].label, fields[0].content)
                in 1 .. Int.MAX_VALUE -> {
                    fields.dropLast(1).forEach { PlainPassLabel(it.label, it.content, Modifier.weight(1f)) }
                    fields.lastOrNull()?.let { PlainPassLabel(it.label, it.content, Modifier.weight(1f), LabelAlign.RIGHT) }
                }
            }
        }
    }
}
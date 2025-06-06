package nz.eloque.foss_wallet.ui.components.card_contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.ui.components.AbbreviatingText
import nz.eloque.foss_wallet.ui.components.DateView
import nz.eloque.foss_wallet.ui.components.LocationButton
import nz.eloque.foss_wallet.ui.components.card_primary.AirlineBoardingPrimary
import nz.eloque.foss_wallet.ui.components.card_primary.GenericBoardingPrimary
import nz.eloque.foss_wallet.ui.components.card_primary.GenericPrimary
import nz.eloque.foss_wallet.ui.components.pass_view.HeaderFieldsView


@Composable
fun GenericContent(
    pass: Pass,
    cardColors: CardColors,
    modifier: Modifier = Modifier,
    content: @Composable ((cardColors: CardColors) -> Unit)
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .weight(5f)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = pass.thumbnailFile(context) ?: pass.iconFile(context),
                        contentDescription = stringResource(R.string.image),
                        modifier = Modifier
                            .padding(5.dp)
                            .width(30.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    AbbreviatingText(
                        text = pass.logoText ?: "",
                        maxLines = 1,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
                HeaderFieldsView(
                    headerFields = pass.headerFields,
                    cardColors = cardColors
                )
                when (pass.type) {
                    is PassType.Boarding -> when (pass.type.transitType) {
                        TransitType.AIR -> AirlineBoardingPrimary(pass, cardColors)
                        else -> GenericBoardingPrimary(pass, pass.type.transitType, cardColors)
                    }
                    else -> GenericPrimary(pass, cardColors)
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateView(pass.description, pass.relevantDate, pass.expirationDate)
            pass.locations.firstOrNull()?.let { LocationButton(it) }
        }
        content.invoke(cardColors)
    }
}
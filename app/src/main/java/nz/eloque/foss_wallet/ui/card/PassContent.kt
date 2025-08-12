package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.ui.card.primary.AirlineBoardingPrimary
import nz.eloque.foss_wallet.ui.card.primary.GenericBoardingPrimary
import nz.eloque.foss_wallet.ui.card.primary.GenericPrimary
import nz.eloque.foss_wallet.ui.screens.pass.AsyncPassImage


@Composable
fun ShortPassContent(
    pass: Pass,
    cardColors: CardColors,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        HeaderRow(pass)
        when (pass.type) {
            is PassType.Boarding ->
                when (pass.type.transitType) {
                    TransitType.AIR -> AirlineBoardingPrimary(pass, cardColors)
                    else -> GenericBoardingPrimary(pass, pass.type.transitType, cardColors)
                }
            else -> GenericPrimary(pass)
        }
        if (pass.primaryFields.empty() && pass.hasStrip) {
            AsyncPassImage(model = pass.stripFile(context), modifier = Modifier.fillMaxWidth())
        }

        DateLocationRow(pass)
    }
}

private fun List<PassField>.empty(): Boolean {
    return this.isEmpty() || this.all { it.content.isEmpty() }
}

@Composable
fun PassContent(
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
    ) {
        HeaderRow(pass)
        when (pass.type) {
            is PassType.Boarding ->
                when (pass.type.transitType) {
                    TransitType.AIR -> AirlineBoardingPrimary(pass, cardColors)
                    else -> GenericBoardingPrimary(pass, pass.type.transitType, cardColors)
                }
            else -> GenericPrimary(pass)
        }
        AsyncPassImage(model = pass.stripFile(context), modifier = Modifier.fillMaxWidth())
        FieldsRow(pass.secondaryFields)
        FieldsRow(pass.auxiliaryFields)

        content.invoke(cardColors)

        DateLocationRow(pass)
    }
}
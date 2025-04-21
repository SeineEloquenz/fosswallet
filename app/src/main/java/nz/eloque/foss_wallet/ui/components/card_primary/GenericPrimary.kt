package nz.eloque.foss_wallet.ui.components.card_primary

import androidx.compose.material3.CardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.components.pass_view.PassFields

@Composable
fun GenericPrimary(
    pass: Pass,
    cardColors: CardColors,
    modifier: Modifier = Modifier
) {
    if (pass.primaryFields.isNotEmpty()) {
        PassFields(pass.primaryFields, cardColors = cardColors)
    }
}
package nz.eloque.foss_wallet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.components.card_contents.DeutschlandTicketContent
import nz.eloque.foss_wallet.ui.components.card_contents.GenericContent

@Composable
fun PassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    content: @Composable ((cardColors: CardColors) -> Unit),
) {
    val cardColors = pass.colors?.toCardColors() ?: colors
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)
    if (onClick == null) {
        ElevatedCard(
            colors = cardColors,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
        ) {
            Contents(pass, cardColors, content)
        }
    } else {
        ElevatedCard(
            onClick = onClick,
            colors = cardColors,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
        ) {
            Contents(pass, cardColors, content)
        }
    }
}

@Composable
private fun Contents(
    pass: Pass,
    cardColors: CardColors,
    content: @Composable ((cardColors: CardColors) -> Unit),
) {
    when {
        "pass.amcongmbh.mobileticket" == pass.passTypeIdentifier -> DeutschlandTicketContent(
            pass = pass,
            cardColors = cardColors,
            content = content
        )
        else -> GenericContent(
            pass = pass,
            cardColors = cardColors,
            content = content
        )
    }
}

@Preview
@Composable
private fun PasscardPreview() {
    PassCard(
        pass = Pass.placeholder(),
    ) {

    }
}
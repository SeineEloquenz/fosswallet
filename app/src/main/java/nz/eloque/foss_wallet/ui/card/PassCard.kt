package nz.eloque.foss_wallet.ui.card

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

@Composable
fun ShortPassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    colors: CardColors = CardDefaults.elevatedCardColors(),
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
            ShortPassContent(pass, cardColors)
        }
    } else {
        ElevatedCard(
            onClick = onClick,
            colors = cardColors,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
        ) {
            ShortPassContent(pass, cardColors)
        }
    }
}

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
            PassContent(pass, cardColors, Modifier, content)
        }
    } else {
        ElevatedCard(
            onClick = onClick,
            colors = cardColors,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
        ) {
            PassContent(pass, cardColors, Modifier, content)
        }
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
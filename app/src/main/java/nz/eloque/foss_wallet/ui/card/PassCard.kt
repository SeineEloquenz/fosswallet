package nz.eloque.foss_wallet.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.ui.components.SelectionIndicator

@Composable
fun ShortPassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
) {
    val cardColors = if (pass.colors != null) { pass.colors.toCardColors() } 
    else { CardDefaults.elevatedCardColors() }
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ElevatedCard(
            colors = cardColors,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale),
            onClick = onClick,
        ) {
            ShortPassContent(pass, cardColors)
        }

        if (selected) {
            SelectionIndicator(Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
fun PassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    content: @Composable (CardColors) -> Unit = {}
) {
    val cardColors = if (pass.colors != null) { pass.colors.toCardColors() } 
    else { CardDefaults.elevatedCardColors() }
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)
    ElevatedCard(
        colors = cardColors,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        PassContent(pass, cardColors, Modifier)
        content(cardColors)
    }
}

fun PassColors.toCardColors() = CardColors(
    containerColor = background,
    contentColor = foreground,
    disabledContainerColor = background.copy(alpha = 0.38f),
    disabledContentColor = foreground.copy(alpha = 0.38f)
)

/*
@Preview
@Composable
private fun PasscardPreview() {
    PassCard(
        pass = Pass.placeholder(),
    ) {
    }
}
 */

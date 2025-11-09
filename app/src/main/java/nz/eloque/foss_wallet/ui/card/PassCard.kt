package nz.eloque.foss_wallet.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.components.SelectionIndicator

@Composable
fun ShortPassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    colors: CardColors = CardDefaults.elevatedCardColors(),
) {
    val cardColors = pass.colors?.toCardColors()?.adaptToDarkMode() ?: colors
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
    colors: CardColors = CardDefaults.elevatedCardColors(),
    content: @Composable ((cardColors: CardColors) -> Unit),
) {
    val cardColors = pass.colors?.toCardColors()?.adaptToDarkMode() ?: colors
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
        PassContent(pass, cardColors, Modifier, content)
    }
}

@Composable
private fun CardColors.adaptToDarkMode(): CardColors {
    val isDarkMode = isSystemInDarkTheme()
    val isBlackOnWhite = containerColor.isWhite() && contentColor.isBlack()
    val isWhiteOnBlack = containerColor.isBlack() && contentColor.isWhite()
    
    return if (isBlackOnWhite || isWhiteOnBlack) {
        if (isDarkMode) {
            copy(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        } else {
            copy(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        }
    } else {
        this
    }
}

private fun Color.isWhite(): Boolean {
    return red > 0.95f && green > 0.95f && blue > 0.95f
}

private fun Color.isBlack(): Boolean {
    return red < 0.05f && green < 0.05f && blue < 0.05f
}

@Preview
@Composable
private fun PasscardPreview() {
    PassCard(
        pass = Pass.placeholder(),
    ) {

    }
}

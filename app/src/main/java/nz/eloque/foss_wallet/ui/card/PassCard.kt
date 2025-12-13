package nz.eloque.foss_wallet.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
import nz.eloque.foss_wallet.utils.darken


@Composable
fun ShortPassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    toned: Boolean = false
) {
    val cardColors = passCardColors(pass.colors, toned)
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
    content: @Composable () -> Unit = {}
) {
    val cardColors = passCardColors(pass.colors)
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
fun passCardColors(passColors: PassColors?, toned: Boolean = false): CardColors {
    val untonedPassColors = if (passColors != null) {
        CardDefaults.elevatedCardColors(
            containerColor = passColors.background,
            contentColor = passColors.foreground,
            disabledContainerColor = passColors.background.copy(alpha = 0.38f),
            disabledContentColor = passColors.foreground.copy(alpha = 0.38f)
        )
    } else {
        CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
    return if (toned) CardDefaults.elevatedCardColors(
        containerColor = untonedPassColors.containerColor.darken(1.25f),
        contentColor = untonedPassColors.contentColor.darken(1.25f),
        disabledContainerColor = untonedPassColors.disabledContainerColor.darken(1.25f),
        disabledContentColor = untonedPassColors.disabledContentColor.darken(1.25f)
    )
        else untonedPassColors
}

@Preview
@Composable
private fun PasscardPreview() {
    PassCard(
        pass = Pass.placeholder(),
    ) {}
}

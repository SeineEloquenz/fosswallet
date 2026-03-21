package nz.eloque.foss_wallet.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.components.FullscreenBarcode
import nz.eloque.foss_wallet.ui.components.SelectionIndicator
import nz.eloque.foss_wallet.ui.effects.UpdateBrightness
import nz.eloque.foss_wallet.utils.darken


@Composable
fun ShortPassCard(
    pass: LocalizedPassWithTags,
    allTags: Set<Tag>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit,
    selected: Boolean = false,
    increaseBrightness: Boolean = false,
    barcodePosition: BarcodePosition,
    toned: Boolean = false
) {
    val cardColors = passCardColors(pass.pass.colors, toned)
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)
    var showBarcode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        ElevatedCard(
            colors = cardColors,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onDoubleClick = { pass.pass.barCodes.firstOrNull()?.let { showBarcode = true}}
                )
        ) {
            ShortPassContent(
                localizedPass = pass,
                cardColors = cardColors,
                allTags = allTags,
            )
        }
        if (selected) {
            SelectionIndicator(Modifier.align(Alignment.TopEnd))
        }
    }

    pass.pass.barCodes.firstOrNull()?.let { barcode ->
        val image = barcode.encodeAsBitmap(
            if (barcode.is1d()) 3000 else 1000,
            1000,
            pass.pass.renderLegacy && barcode.hasLegacyRepresentation()
        )


        if (showBarcode && increaseBrightness) {
            UpdateBrightness()
        }
        FullscreenBarcode(
            image = image,
            barcodePosition = barcodePosition,
            isFullscreen = showBarcode,
            onDismiss = { showBarcode = !showBarcode },
        )
    }
}

@Composable
fun PassCard(
    localizedPass: LocalizedPassWithTags,
    allTags: Set<Tag>,
    onTagClick: (Tag) -> Unit,
    onTagAdd: (Tag) -> Unit,
    onTagCreate: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    val pass = localizedPass.pass

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
        PassContent(
            localizedPass = localizedPass,
            cardColors = cardColors,
            allTags = allTags,
            onTagClick = onTagClick,
            onTagAdd = onTagAdd,
            onTagCreate = onTagCreate,
            content = content,
        )
    }
}

@Composable
fun passCardColors(passColors: PassColors?, toned: Boolean = false): CardColors {
    val untonedPassColors = passColors?.toCardColors()
        ?: CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    return if (toned) CardDefaults.elevatedCardColors(
        containerColor = untonedPassColors.containerColor.darken(1.25f),
        contentColor = untonedPassColors.contentColor.darken(1.25f),
        disabledContainerColor = untonedPassColors.disabledContainerColor.darken(1.25f),
        disabledContentColor = untonedPassColors.disabledContentColor.darken(1.25f)
    ) else untonedPassColors
}

@Preview
@Composable
private fun PasscardPreview() {
    PassCard(
        localizedPass = LocalizedPassWithTags.placeholder(),
        allTags = setOf(Tag("Tag 1", Color(0, 0, 0)), Tag("Tag 2", Color(100, 100, 100))),
        onTagClick = {},
        onTagAdd = {},
        onTagCreate = {}
    ) {}
}

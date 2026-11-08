package nz.eloque.foss_wallet.ui.card

import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import nz.eloque.foss_wallet.model.field.TextAlignment

/** The alignment a field requests, or null for natural alignment, which keeps the layout's default. */
fun TextAlignment.horizontalAlignment(): Alignment.Horizontal? =
    when (this) {
        TextAlignment.LEFT -> AbsoluteAlignment.Left
        TextAlignment.CENTER -> Alignment.CenterHorizontally
        TextAlignment.RIGHT -> AbsoluteAlignment.Right
        TextAlignment.NATURAL -> null
    }

fun Alignment.Horizontal.textAlign(): TextAlign =
    when (this) {
        Alignment.End -> TextAlign.End
        Alignment.CenterHorizontally -> TextAlign.Center
        AbsoluteAlignment.Left -> TextAlign.Left
        AbsoluteAlignment.Right -> TextAlign.Right
        else -> TextAlign.Start
    }

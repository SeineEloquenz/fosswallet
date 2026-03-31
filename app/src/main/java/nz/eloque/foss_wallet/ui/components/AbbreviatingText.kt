package nz.eloque.foss_wallet.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AbbreviatingText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    useTooltip: Boolean = true
) {
    var hasOverflow by remember { mutableStateOf(false) }
    val content = @Composable {
        Text(
            text = text,
            modifier = modifier,
            textAlign = textAlign,
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines,
            onTextLayout = { if (useTooltip) hasOverflow = it.hasVisualOverflow },
            style = style
        )
    }

    if (hasOverflow) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text(text) } },
            state = rememberTooltipState(),
            content = content
        )
    } else content()
}

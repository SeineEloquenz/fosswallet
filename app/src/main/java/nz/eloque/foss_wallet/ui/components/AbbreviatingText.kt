package nz.eloque.foss_wallet.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbbreviatingText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
) {
    var isEllipsized by remember { mutableStateOf(false) }
    val toolTipState = rememberTooltipState()

    if (isEllipsized) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            state = toolTipState,
            tooltip = {
                PlainTooltip { Text(text) }
            },
            modifier = modifier
        ) {
            Text(
                text = text,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                style = style,
                textAlign = textAlign,
                onTextLayout = { result ->
                    isEllipsized = result.hasVisualOverflow
                }
            )
        }
    } else {
        Text(
            text = text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = style,
            textAlign = textAlign,
            modifier = modifier,
            onTextLayout = { result ->
                isEllipsized = result.hasVisualOverflow
            }
        )
    }
}
package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.ui.components.AbbreviatingText

private val linkStyle = TextLinkStyles(
    style = SpanStyle(
        textDecoration = TextDecoration.Underline,
        fontStyle = FontStyle.Italic,
    )
)

enum class LabelAlign(val textAlign: TextAlign, val horizontalAlignment: Alignment.Horizontal, val horizontalArrangement: Arrangement.Horizontal) {
    LEFT(TextAlign.Start, Alignment.Start, Arrangement.Start),
    RIGHT(TextAlign.End, Alignment.End, Arrangement.End),
}

@Composable
fun MainLabel(
    label: String?,
    content: PassContent
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        if (!label.isNullOrEmpty()) {
            AbbreviatingText(
                text = label,
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        SelectionContainer {
            val contentString = content.prettyPrint()
            Text(
                text = contentString,
                minLines = 2,
                maxLines = 2,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
fun ElevatedPassLabel(
    label: String?,
    content: PassContent,
    modifier: Modifier = Modifier,
    labelAlign: LabelAlign = LabelAlign.LEFT,
    colors: CardColors = CardDefaults.outlinedCardColors(),
) {
    ElevatedCard(
        colors = colors,
        modifier = modifier,
    ) {
        PassLabelContents(label, content, Modifier, labelAlign)
    }
}

@Composable
fun OutlinedPassLabel(
    label: String?,
    content: PassContent,
    modifier: Modifier = Modifier,
    labelAlign: LabelAlign = LabelAlign.LEFT,
    colors: CardColors = CardDefaults.outlinedCardColors(),
) {
    OutlinedCard(
        colors = colors,
        modifier = modifier,
    ) {
        PassLabelContents(label, content, Modifier, labelAlign)
    }
}

@Composable
fun PlainPassLabel(
    label: String?,
    content: PassContent,
    modifier: Modifier = Modifier,
    labelAlign: LabelAlign = LabelAlign.LEFT,
) {
    PassLabelContents(label, content, modifier, labelAlign)
}

@Composable
private fun PassLabelContents(
    label: String?,
    content: PassContent,
    modifier: Modifier = Modifier,
    labelAlign: LabelAlign = LabelAlign.LEFT,
) {
    Row(
        horizontalArrangement = labelAlign.horizontalArrangement,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = labelAlign.horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .padding(12.dp)
        ) {
            if (!label.isNullOrEmpty()) {
                AbbreviatingText(
                    text = label,
                    maxLines = 1,
                    textAlign = labelAlign.textAlign,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            SelectionContainer {
                val contentString = content.prettyPrint().sanitize()
                Text(
                    text = if (contentString.isNotEmpty()) {
                        AnnotatedString.fromHtml(contentString, linkStyle) } else { AnnotatedString("-") },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = labelAlign.textAlign,
                )
            }
        }
    }
}

private fun String.sanitize(): String {
    return this
        .replace("\r", "")
        .replace("\\r", "")
        .replace("\n", "<br>")
}

@Preview
@Composable
private fun PassLabelPreview() {
    OutlinedPassLabel(
        label = "GRP",
        content = PassContent.Plain("34")
    )
}

@Preview
@Composable
private fun LongPassLabelPreview() {
    OutlinedPassLabel(
        label = "Information",
        content = PassContent.Plain("""
            This is a long text.
            
            Possibly multiple lines
            
            this should break
            nicely and
            format like a real text input field lololololol
            dadadwa dwa
             d
             wad 
             wa d
             wa dwa
             
             Lorem Ipsum
        """.trimIndent())
    )
}

@Preview
@Composable
private fun HtmlPassLabelPreview() {
    OutlinedPassLabel(
        label = "Information",
        content = PassContent.Plain("Panne und Unfall telefonisch melden\r\n👉 Pannenhilfe Deutschland: <a href=\"tel:+498920204000\">089 20 20 40 00</a>\r\n👉 Pannenhilfe Ausland: <a href=\"tel:+4989222222\">+49 89 22 22 22</a> \r\n\r\nPanne oder Unfall bequem online melden \r\n👉 <a href=\"https://www.adac.de/der-adac/verein/pannenhilfe/pannenhilfe-online/eingabeseite/\">Panne melden</a>\r\n\r\nAmbulanz-Service \r\n<a href=\"tel:+4989767676\">+49 89 76 76 76</a>\r\nbei akuten Erkrankungen und Verletzungen\r\n".trimIndent())
    )
}
package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val linkStyle = TextLinkStyles(
    style = SpanStyle(
        textDecoration = TextDecoration.Underline,
        fontStyle = FontStyle.Italic,
    )
)

@Composable
fun ElevatedPassLabel(
    label: String,
    content: PassContent,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
) {
    ElevatedCard(
        colors = colors,
        modifier = modifier,
    ) {
        PassLabelContents(label, content, Modifier.fillMaxSize())
    }
}

@Composable
fun OutlinedPassLabel(
    label: String,
    content: PassContent,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
) {
    OutlinedCard(
        colors = colors,
        modifier = modifier,
    ) {
        PassLabelContents(label, content, Modifier.fillMaxSize())
    }
}

@Composable
fun DTPassLabel(
    label: String,
    content: PassContent,
    modifier: Modifier = Modifier,
    labelTextAlign: TextAlign? = null,
) {
    PassLabelContents(label, content, modifier, labelTextAlign)
}

@Composable
private fun PassLabelContents(
    label: String,
    content: PassContent,
    modifier: Modifier,
    labelTextAlign: TextAlign? = null,
) {
    Box(
        modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .padding(12.dp)
        ) {
            if (label.isNotEmpty()) {
                AbbreviatingText(
                    text = label,
                    maxLines = 1,
                    textAlign = labelTextAlign,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            SelectionContainer {
                val contentString = content.prettyPrint()
                Text(
                    text = if (contentString.isNotEmpty()) {
                        AnnotatedString.fromHtml(contentString, linkStyle) } else { AnnotatedString("-") },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
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
        content = PassContent.Plain("""
            <p>
            This is a paragraph
            </p>
            This should <b> bold. </b>
            <a href="https://example.com">This is a link</a>
             Lorem Ipsum
        """.trimIndent())
    )
}
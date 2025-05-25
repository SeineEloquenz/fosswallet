package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val linkStyle = TextLinkStyles(
    style = SpanStyle(
        textDecoration = TextDecoration.Underline,
        fontStyle = FontStyle.Italic,
    )
)

@Composable
fun PassLabel(
    label: String,
    content: String,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.outlinedCardColors(),
) {
    OutlinedCard(
        colors = colors,
        modifier = modifier,
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                if (label.isNotEmpty()) {
                    Text(
                        text = label,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                SelectionContainer {
                    Text(
                        text = if (content.isNotEmpty()) {
                            AnnotatedString.fromHtml(content, linkStyle) } else { AnnotatedString("-") },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PassLabelPreview() {
    PassLabel(
        label = "GRP",
        content = "34"
    )
}

@Preview
@Composable
private fun LongPassLabelPreview() {
    PassLabel(
        label = "Information",
        content = """
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
        """.trimIndent()
    )
}

@Preview
@Composable
private fun HtmlPassLabelPreview() {
    PassLabel(
        label = "Information",
        content = """
            <p>
            This is a paragraph
            </p>
            This should <b> bold. </b>
            <a href="https://example.com">This is a link</a>
             Lorem Ipsum
        """.trimIndent()
    )
}
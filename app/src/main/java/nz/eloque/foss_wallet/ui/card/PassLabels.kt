package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.ui.components.FairRow
import nz.eloque.foss_wallet.ui.components.findMaxAllowedWidth

private val linkStyle = TextLinkStyles(
    style = SpanStyle(
        textDecoration = TextDecoration.Underline,
        fontStyle = FontStyle.Italic,
    )
)

val passFieldContentStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyMedium.copy(
        lineHeight = TextUnit.Unspecified,
        hyphens = Hyphens.Auto
    )

@Composable
fun PassFieldLabel(
    text: String?
) {
    Text(
        text = text?.uppercase(LocalLocale.current.platformLocale).orEmpty(),
        fontWeight = FontWeight.Bold,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun PassField(
    field: PassField,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1,
    style: TextStyle = passFieldContentStyle,
    isSelectable: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        PassFieldLabel(field.label)
        val content = @Composable {
            Text(
                text = field.content.parseHtml(),
                fontSize = fontSize,
                overflow = TextOverflow.Ellipsis,
                maxLines = maxLines,
                style = style
            )
        }
        if (isSelectable) SelectionContainer { content() } else content()
    }
}

/**
 * Composable function that calculates the maximum font size for the content strings in a row
 * of [PassField]. A binary search algorithm is used to find the optimal font size that fits in the
 * available space.
 *
 * @param fields The list of [PassField] for which the maximum font size is calculated.
 * @param modifier The modifier to be applied to the layout.
 * @param minFontSize The smallest allowed font size.
 * @param maxFontSize The largest allowed font size.
 * @param stepSize The step size for the list of potential font sizes.
 * @param maxLines The maximum number of lines for [PassField.content].
 * @param spacing The space between each item in the row.
 * @param useFixedWidth When `false`, uses [findMaxAllowedWidth] for the maximum allowed width of
 *   the items in a [FairRow]. Otherwise each item gets an equal share of the available width.
 * @param content The content of the layout.
 */
@Composable
fun AutoSizePassFields(
    fields: List<PassField>,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 10.sp,
    maxFontSize: TextUnit = 100.sp,
    stepSize: TextUnit = 0.5.sp,
    maxLines: Int = 1,
    spacing: Dp = 0.dp,
    useFixedWidth: Boolean = false,
    content: @Composable (fontSize: TextUnit) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        if (fields.isEmpty()) return@BoxWithConstraints content(TextUnit.Unspecified)

        val density = LocalDensity.current
        val locale = LocalLocale.current.platformLocale
        val labelStyle = MaterialTheme.typography.labelMedium
        val contentStyle = passFieldContentStyle
        val textMeasurer = rememberTextMeasurer(0)

        // sort content by length to skip measuring the other fields in case of a visual overflow
        val (labels, contents) = fields
            .map { Pair(it.label?.uppercase(locale).orEmpty(), it.content.parseHtml()) }
            .sortedByDescending { it.second.length }
            .unzip()

        val (labelHeight, labelWidths) = remember(labels) {
            val n = if (useFixedWidth) 1 else labels.size
            val results = labels.take(n).map { textMeasurer.measure(text = it, style = labelStyle) }
            Pair(results.first().size.height, results.map { it.size.width })
        }

        val finalFontSize = remember(constraints, contents, labelHeight, labelWidths) {
            val spacingPx = with(density) { spacing.roundToPx() }
            val availableWidth = (constraints.maxWidth - spacingPx * (fields.size - 1)).coerceAtLeast(0)
            val contentHeight = (constraints.maxHeight - labelHeight).coerceAtLeast(0)

            val maxFontSizeLimit = with(density) {
                if (maxFontSize.toPx() < contentHeight) maxFontSize else contentHeight.toSp() / maxLines
            }
            val fontSizes = generateSequence(minFontSize) { item ->
                (item.value + stepSize.value).sp.takeIf { it <= maxFontSizeLimit }
            }.toList()

            var maxItemWidth = if (useFixedWidth)
                availableWidth / fields.size
            else
                findMaxAllowedWidth(labelWidths, availableWidth)
            fontSizes.binarySearch { currentFontSize ->
                val contentWidths = contents.map {
                    val result = textMeasurer.measure(
                        text = it,
                        style = contentStyle.copy(fontSize = currentFontSize),
                        maxLines = maxLines,
                        constraints = Constraints(maxHeight = contentHeight, maxWidth = maxItemWidth)
                    )
                    if (result.hasVisualOverflow) return@binarySearch true else result.size.width
                }
                if (useFixedWidth) return@binarySearch false

                val itemWidths = labelWidths.zip(contentWidths) { a, b -> maxOf(a, b) }
                maxItemWidth = findMaxAllowedWidth(itemWidths, availableWidth)
                contentWidths.any { it > maxItemWidth }
            }
        }

        content(finalFontSize)
    }
}

/**
 * Searches the list for the largest possible element for which [moveBackwards] is `false` using a
 * binary search algorithm.
 *
 * @param moveBackwards Function that returns `true` if the search should move to smaller elements
 *   and `false` otherwise.
 * @return The largest element for with [moveBackwards] is `false` or the smallest one if
 *   [moveBackwards] is `true` for all elements.
 */
fun <T> List<T>.binarySearch(moveBackwards: (T) -> Boolean): T {
    var lowIndex = 0
    var highIndex = size - 1
    while (lowIndex <= highIndex) {
        val midIndex = (lowIndex + highIndex) / 2
        if (moveBackwards(get(midIndex)))
            highIndex = midIndex - 1
        else
            lowIndex = midIndex + 1
    }
    return get(highIndex.coerceAtLeast(0))
}

private fun String.sanitize(): String {
    return this
        .replace("\r", "")
        .replace("\\r", "")
        .replace("\n", "<br>")
}

private fun PassContent.parseHtml(): AnnotatedString =
    AnnotatedString.fromHtml(prettyPrint().sanitize(), linkStyle)

private fun previewPassField(label: String, content: String) =
    PassField("", label, PassContent.Plain(content))

@Preview(showBackground = true)
@Composable
private fun PreviewPassFieldBack() {
    PassField(
        field = previewPassField(
            "Information",
            """
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
        ),
        maxLines = Int.MAX_VALUE
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewPassFieldBackHtml() {
    PassField(
        field = previewPassField(
            "Information",
            "Panne und Unfall telefonisch melden\r\n👉 Pannenhilfe Deutschland: <a href=\"tel:+498920204000\">089 20 20 40 00</a>\r\n👉 Pannenhilfe Ausland: <a href=\"tel:+4989222222\">+49 89 22 22 22</a> \r\n\r\nPanne oder Unfall bequem online melden \r\n👉 <a href=\"https://www.adac.de/der-adac/verein/pannenhilfe/pannenhilfe-online/eingabeseite/\">Panne melden</a>\r\n\r\nAmbulanz-Service \r\n<a href=\"tel:+4989767676\">+49 89 76 76 76</a>\r\nbei akuten Erkrankungen und Verletzungen\r\n".trimIndent()
        ),
        maxLines = Int.MAX_VALUE
    )
}

@Preview(showBackground = true, widthDp = 300)
@Preview(showBackground = true, widthDp = 200)
@Composable
private fun PreviewAutoSizePassFields() {
    val passFieldsData = listOf(
        listOf(
            previewPassField("AAA ".repeat(4), "A"),
            previewPassField("BBB ".repeat(4), "B"),
        ),
        listOf(
            previewPassField("AAA", "AAA ".repeat(5)),
            previewPassField("BBB", "BBB ".repeat(5)),
        ),
        listOf(
            previewPassField("AAA", "A"),
            previewPassField("BBB", "BBB ".repeat(6)),
        ),
        listOf(
            previewPassField("AAA", "AAA ".repeat(6)),
            previewPassField("BBB", "B"),
        ),
        listOf(
            previewPassField("AAA ".repeat(6), "AAA ".repeat(6)),
            previewPassField("BBB", "B"),
        ),
        listOf(
            previewPassField("AAA ".repeat(6), "AAA ".repeat(6)),
            previewPassField("BBB ".repeat(6), "B"),
        ),
    )
    Column {
        passFieldsData.forEach {
            FieldsRow(
                fields = it,
                modifier = Modifier.border(Dp.Hairline, Color.Magenta)
            )
        }
    }
}

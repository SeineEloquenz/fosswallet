package nz.eloque.foss_wallet.ui.card

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.LocaleList
import android.view.textclassifier.TextClassification
import android.view.textclassifier.TextClassifier
import android.view.textclassifier.TextLinks
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import nz.eloque.foss_wallet.model.field.DataDetectorType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataDetectionTest {
    private val styles = TextLinkStyles()

    private class FakeClassifier(
        private val spans: List<IntRange>,
    ) : TextClassifier {
        var requestedTypes: Collection<String>? = null

        override fun generateLinks(request: TextLinks.Request): TextLinks {
            requestedTypes = request.entityConfig?.resolveEntityListModifications(emptyList())
            return TextLinks
                .Builder(request.text.toString())
                .also { builder -> spans.forEach { builder.addLink(it.first, it.last + 1, mapOf(TextClassifier.TYPE_PHONE to 1f)) } }
                .build()
        }

        override fun classifyText(
            text: CharSequence,
            startIndex: Int,
            endIndex: Int,
            defaultLocales: LocaleList?,
        ): TextClassification {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val intent = PendingIntent.getActivity(context, 0, Intent(), PendingIntent.FLAG_IMMUTABLE)
            val action = RemoteAction(Icon.createWithResource(context, android.R.drawable.ic_menu_call), "Call", "Call", intent)
            return TextClassification.Builder().addAction(action).build()
        }
    }

    private fun AnnotatedString.links() = getLinkAnnotations(0, length).map { it.start until it.end }

    @Test
    fun `links detected spans`() {
        val text = AnnotatedString("Call 0800 123 456")
        val detected = runBlocking { FakeClassifier(listOf(5..16)).detectLinks(text, setOf(DataDetectorType.PHONE_NUMBER), styles) }

        assertEquals(listOf(5 until 17), detected.links())
        assertEquals("Call", (detected.getLinkAnnotations(0, detected.length).single().item as LinkAnnotation.Clickable).tag)
    }

    @Test
    fun `requests only the entity types of the enabled detectors`() {
        val classifier = FakeClassifier(emptyList())
        runBlocking {
            classifier.detectLinks(
                AnnotatedString("text"),
                setOf(DataDetectorType.LINK, DataDetectorType.CALENDAR_EVENT),
                styles,
            )
        }

        assertEquals(
            setOf(TextClassifier.TYPE_URL, TextClassifier.TYPE_EMAIL, TextClassifier.TYPE_DATE, TextClassifier.TYPE_DATE_TIME),
            classifier.requestedTypes?.toSet(),
        )
    }

    @Test
    fun `detects nothing when detectors are disabled`() {
        val classifier = FakeClassifier(listOf(0..3))
        val detected = runBlocking { classifier.detectLinks(AnnotatedString("0800"), emptySet(), styles) }

        assertEquals(emptyList<IntRange>(), detected.links())
        assertNull(classifier.requestedTypes)
    }

    @Test
    fun `keeps existing links and skips overlapping detections`() {
        val text =
            buildAnnotatedString {
                append("Call ")
                withLink(LinkAnnotation.Url("tel:0800")) { append("0800") }
                append(" or 0900")
            }
        val detected = runBlocking { FakeClassifier(listOf(5..8, 13..16)).detectLinks(text, setOf(DataDetectorType.PHONE_NUMBER), styles) }

        assertEquals(listOf(5 until 9, 13 until 17), detected.links().sortedBy { it.first })
    }
}

package nz.eloque.foss_wallet.ui.card

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.RemoteAction
import android.os.Build
import android.util.Log
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextClassifier
import android.view.textclassifier.TextLinks
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.model.field.DataDetectorType

private const val TAG = "DataDetection"

@Composable
fun rememberDetectedLinks(
    text: AnnotatedString,
    types: Set<DataDetectorType>,
    styles: TextLinkStyles,
): AnnotatedString {
    val context = LocalContext.current
    val detected by produceState(text, text, types) {
        if (types.isEmpty()) return@produceState
        val classifier = context.getSystemService(TextClassificationManager::class.java)?.textClassifier ?: return@produceState
        value = classifier.detectLinks(text, types, styles)
    }
    return detected
}

/** Adds a link for every detected span of [types] that does not overlap an existing link. */
suspend fun TextClassifier.detectLinks(
    text: AnnotatedString,
    types: Set<DataDetectorType>,
    styles: TextLinkStyles,
): AnnotatedString {
    val entityTypes = types.flatMap { it.entityTypes() }
    if (entityTypes.isEmpty() || text.isBlank() || text.length > maxGenerateLinksTextLength) return text
    val request =
        TextLinks.Request
            .Builder(text.text)
            .setEntityConfig(TextClassifier.EntityConfig.createWithExplicitEntityList(entityTypes))
            .build()
    val existing = text.getLinkAnnotations(0, text.length)
    return withContext(Dispatchers.Default) {
        val links =
            generateLinks(request).links.filter { link ->
                existing.none { it.start < link.end && link.start < it.end }
            }
        buildAnnotatedString {
            append(text)
            links.forEach { link ->
                val action = classifyText(text.text, link.start, link.end, null).actions.firstOrNull() ?: return@forEach
                addLink(LinkAnnotation.Clickable(action.title.toString(), styles) { action.send() }, link.start, link.end)
            }
        }
    }
}

private fun DataDetectorType.entityTypes(): List<String> =
    when (this) {
        DataDetectorType.PHONE_NUMBER -> listOf(TextClassifier.TYPE_PHONE)
        DataDetectorType.LINK -> listOf(TextClassifier.TYPE_URL, TextClassifier.TYPE_EMAIL)
        DataDetectorType.ADDRESS -> listOf(TextClassifier.TYPE_ADDRESS)
        DataDetectorType.CALENDAR_EVENT -> listOf(TextClassifier.TYPE_DATE, TextClassifier.TYPE_DATE_TIME)
    }

private fun RemoteAction.send() {
    // The classifier's PendingIntents belong to another app, so starting an activity needs our foreground privilege.
    val options =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityOptions
                .makeBasic()
                .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else {
            null
        }
    try {
        actionIntent.send(null, 0, null, null, null, null, options)
    } catch (e: PendingIntent.CanceledException) {
        Log.w(TAG, "Link action was cancelled", e)
    }
}

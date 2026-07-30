package nz.eloque.foss_wallet.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.webkit.MimeTypeMap
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.createBitmap
import nz.eloque.foss_wallet.model.field.PassDateTime
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.LinkedList

fun <T> JSONArray.map(action: (JSONObject) -> T): List<T> {
    val list: MutableList<T> = LinkedList()
    this.forEach { list.add(action(it)) }
    return list
}

fun JSONArray.filter(predicate: (JSONObject) -> Boolean): JSONArray {
    val result = JSONArray()
    this.forEach { if (predicate(it)) result.put(it) }
    return result
}

fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    for (i in 0 until this.length()) {
        action(this.getJSONObject(i))
    }
}

fun PassDateTime.prettyDateTime(
    style: FormatStyle = FormatStyle.SHORT,
    isRelative: Boolean = false,
): String = this.pretty(DateTimeFormatter.ofLocalizedDateTime(style), isRelative)

fun PassDateTime.prettyDate(
    style: FormatStyle = FormatStyle.SHORT,
    isRelative: Boolean = false,
): String = this.pretty(DateTimeFormatter.ofLocalizedDate(style), isRelative)

fun PassDateTime.prettyTime(
    style: FormatStyle = FormatStyle.SHORT,
    isRelative: Boolean = false,
): String = this.pretty(DateTimeFormatter.ofLocalizedTime(style), isRelative)

private fun PassDateTime.pretty(
    formatter: DateTimeFormatter,
    isRelative: Boolean = false,
): String {
    val zone = ZoneId.systemDefault()
    if (isRelative) {
        return DateUtils.getRelativeTimeSpanString(this.toInstant(zone).toEpochMilli()).toString()
    }
    return this.zonedAt(zone).format(formatter)
}

fun ZonedDateTime.prettyDate(style: FormatStyle = FormatStyle.SHORT): String =
    this.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDate(style))

fun ZonedDateTime.prettyDateTime(style: FormatStyle = FormatStyle.SHORT): String =
    this.withZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(style))

fun Color.darken(factor: Float = 0.3f): Color =
    copy(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha,
    )

fun InputStream.toByteArray(): ByteArray {
    val baos = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int
    while ((this.read(buffer).also { len = it }) > -1) {
        baos.write(buffer, 0, len)
    }
    baos.flush()
    return baos.toByteArray()
}

fun JSONObject.stringOrNull(key: String): String? = if (this.has(key)) this.getString(key) else null

infix fun <T : CharSequence> T.inIgnoreCase(charSequence: T?): Boolean = charSequence?.contains(this, ignoreCase = true) == true

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) {
        mutableIntStateOf(firstVisibleItemIndex)
    }
    var previousScrollOffset by remember(this) {
        mutableIntStateOf(firstVisibleItemScrollOffset)
    }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

fun Throwable.asString(): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    this.printStackTrace(pw)
    return sw.toString()
}

fun Drawable.toBitmap(
    width: Int,
    height: Int,
): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, width, height)
    this.draw(canvas)
    return bitmap
}

fun File.getMimeType(): String {
    val extension = this.extension.lowercase()

    return MimeTypeMap
        .getSingleton()
        .getMimeTypeFromExtension(extension)
        ?: "application/octet-stream"
}

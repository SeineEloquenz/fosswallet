package nz.eloque.foss_wallet.utils

import android.text.format.DateUtils
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.LinkedList


fun <T> JSONArray.map(action: (JSONObject) -> T): List<T> {
    val list: MutableList<T> = LinkedList()
    this.forEach { list.add(action.invoke(it)) }
    return list
}

fun JSONArray.forEach(action: (JSONObject) -> Unit) {
    var i = 0
    while (i < this.length()) {
        val element = this.getJSONObject(i)
        action.invoke(element)
        i++
    }
}

fun Instant.prettyDateTime(style: FormatStyle = FormatStyle.SHORT, ignoresTimezone: Boolean = false, isRelative: Boolean = false): String {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(style)
    return this.pretty(dateFormatter, ignoresTimezone, isRelative)
}

fun Instant.prettyDate(style: FormatStyle = FormatStyle.SHORT, ignoresTimezone: Boolean = false, isRelative: Boolean = false): String {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(style)
    return this.pretty(dateFormatter, ignoresTimezone, isRelative)
}

fun Instant.prettyTime(style: FormatStyle = FormatStyle.SHORT, ignoresTimezone: Boolean = false, isRelative: Boolean = false): String {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(style)
    return this.pretty(dateFormatter, ignoresTimezone, isRelative)
}

private fun Instant.pretty(dateFormatter: DateTimeFormatter, ignoresTimezone: Boolean = false, isRelative: Boolean = false): String {
    if (isRelative) {
        return DateUtils.getRelativeTimeSpanString(this.toEpochMilli()).toString()
    }
    return if (ignoresTimezone) {
        val time = ZonedDateTime.ofInstant(this, ZoneId.of("UTC"))
        time.format(dateFormatter)
    } else {
        val zonedTime = ZonedDateTime.ofInstant(this, ZoneId.systemDefault())
        zonedTime.format(dateFormatter)
    }
}

fun Color.darken(factor: Float = 0.3f): Color {
    return copy(
        red = red * factor,
        green = green * factor,
        blue = blue * factor,
        alpha = alpha
    )
}

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

fun JSONObject.stringOrNull(key: String): String? {
    return if (this.has(key)) this.getString(key) else null
}

infix fun <T : CharSequence> T.inIgnoreCase(charSequence: T?): Boolean {
    return charSequence?.contains(this, ignoreCase = true) == true
}

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
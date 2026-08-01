package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassDateTime.Companion.ignoringTimeZone
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.utils.stringOrNull
import org.json.JSONObject
import java.time.format.FormatStyle

object FieldParser {
    fun parse(field: JSONObject): PassField {
        val key = field.getString("key")
        val label = field.stringOrNull("label")
        val value =
            if (field.has("attributedValue")) {
                field.getString("attributedValue")
            } else if (field.has("value")) {
                field.getString("value")
            } else {
                "-"
            }
        val changeMessage = if (field.has("changeMessage")) field.getString("changeMessage") else null

        val parsedDate =
            if (field.hasDateStyle() || field.hasTimeStyle()) {
                TimeParser.parseOrNull(value)?.let { if (field.ignoresTimezone()) it.ignoringTimeZone() else it }
            } else {
                null
            }

        val content =
            when {
                field.has("currencyCode") -> {
                    PassContent.Currency(value, field.getString("currencyCode"))
                }

                parsedDate == null -> {
                    PassContent.Plain(value)
                }

                field.hasDateStyle() && field.hasTimeStyle() -> {
                    PassContent.DateTime(
                        parsedDate,
                        chooseBetter(field.getDateStyle(), field.getTimeStyle()),
                        field.isRelative(),
                    )
                }

                field.hasDateStyle() -> {
                    PassContent.Date(
                        parsedDate,
                        field.getDateStyle(),
                        field.isRelative(),
                    )
                }

                else -> {
                    PassContent.Time(
                        parsedDate,
                        field.getTimeStyle(),
                        field.isRelative(),
                    )
                }
            }

        return PassField(key, label, content, changeMessage)
    }

    private fun String.toFormatStyle(): FormatStyle =
        when (this) {
            "PKDateStyleShort" -> FormatStyle.SHORT
            "PKDateStyleMedium" -> FormatStyle.MEDIUM
            "PKDateStyleLong" -> FormatStyle.LONG
            "PKDateStyleFull" -> FormatStyle.FULL
            else -> FormatStyle.FULL
        }

    private fun JSONObject.hasDateStyle() = hasStyle("dateStyle")

    private fun JSONObject.hasTimeStyle() = hasStyle("timeStyle")

    private fun JSONObject.getDateStyle() = getString("dateStyle").toFormatStyle()

    private fun JSONObject.getTimeStyle() = getString("timeStyle").toFormatStyle()

    private fun JSONObject.ignoresTimezone() = optBoolean("ignoresTimeZone")

    private fun JSONObject.isRelative() = optBoolean("isRelative")

    private fun JSONObject.hasStyle(key: String): Boolean = this.has(key) && this.getString(key) != "PKDateStyleNone"

    private fun chooseBetter(
        left: FormatStyle,
        right: FormatStyle,
    ): FormatStyle = if (left.ordinal >= right.ordinal) left else right
}

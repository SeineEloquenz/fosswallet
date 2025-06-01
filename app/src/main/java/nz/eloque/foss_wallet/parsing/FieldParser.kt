package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import org.json.JSONObject
import java.time.format.FormatStyle

object FieldParser {

    fun parse(field: JSONObject): PassField {
        val key = field.getString("key")
        val label = field.getString("label")
        val value = field.getString("value")

        val content = when {
            field.has("currencyCode") -> PassContent.Currency(value, field.getString("currencyCode"))
            field.has("dateStyle") && field.getString("dateStyle") != "PKDateStyleNone" -> PassContent.Date(value, field.getString("dateStyle").toFormatStyle())
            field.has("timeStyle") && field.getString("timeStyle") != "PKDateStyleNone" -> PassContent.Time(value, field.getString("timeStyle").toFormatStyle())
            else -> PassContent.Plain(value)
        }

        return PassField(key, label, content)
    }

    private fun String.toFormatStyle(): FormatStyle {
        return when (this) {
            "PKDateStyleShort" -> FormatStyle.SHORT
            "PKDateStyleMedium" -> FormatStyle.MEDIUM
            "PKDateStyleLong" -> FormatStyle.LONG
            "PKDateStyleFull" -> FormatStyle.FULL
            else -> FormatStyle.FULL
        }
    }
}
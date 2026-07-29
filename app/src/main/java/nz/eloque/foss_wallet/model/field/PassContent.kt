package nz.eloque.foss_wallet.model.field

import nz.eloque.foss_wallet.parsing.TimeParser
import nz.eloque.foss_wallet.utils.inIgnoreCase
import nz.eloque.foss_wallet.utils.linkify
import nz.eloque.foss_wallet.utils.prettyDate
import nz.eloque.foss_wallet.utils.prettyDateTime
import nz.eloque.foss_wallet.utils.prettyTime
import java.time.format.FormatStyle

sealed class PassContent(
    val id: Int,
) {
    data class Plain(
        val text: String,
    ) : PassContent(PLAIN) {
        override fun contains(query: String) = query inIgnoreCase text

        override fun prettyPrint(): String = linkify(text)

        override fun isEmpty(): Boolean = text.isEmpty()
    }

    data class Currency(
        val amount: String,
        val currency: String,
    ) : PassContent(CURRENCY) {
        override fun contains(query: String) = query inIgnoreCase amount || query inIgnoreCase currency

        override fun prettyPrint(): String = amount + toCurrency(currency)

        override fun isEmpty(): Boolean = amount.isEmpty()

        private fun toCurrency(currencyCode: String): String? =
            try {
                java.util.Currency
                    .getInstance(currency)
                    .symbol
            } catch (_: IllegalArgumentException) {
                " $currencyCode"
            }
    }

    data class Date(
        val date: PassDateTime,
        val format: FormatStyle,
        val isRelative: Boolean,
    ) : PassContent(DATE) {
        override fun contains(query: String) = query inIgnoreCase prettyPrint()

        override fun prettyPrint(): String = date.prettyDate(format, isRelative)

        override fun isEmpty(): Boolean = false
    }

    data class Time(
        val time: PassDateTime,
        val format: FormatStyle,
        val isRelative: Boolean,
    ) : PassContent(TIME) {
        override fun contains(query: String) = query inIgnoreCase prettyPrint()

        override fun prettyPrint(): String = time.prettyTime(format, isRelative)

        override fun isEmpty(): Boolean = false
    }

    data class DateTime(
        val dateTime: PassDateTime,
        val format: FormatStyle,
        val isRelative: Boolean,
    ) : PassContent(DATE_TIME) {
        override fun contains(query: String) = query inIgnoreCase prettyPrint()

        override fun prettyPrint(): String = dateTime.prettyDateTime(format, isRelative)

        override fun isEmpty(): Boolean = false
    }

    companion object {
        const val PLAIN = 0
        const val CURRENCY = 1
        const val DATE = 2
        const val TIME = 3
        const val DATE_TIME = 4

        fun deserialize(content: String): PassContent {
            if (content.length < 2 || !content[0].isDigit() || content[1] != '|') {
                return Plain(content)
            }
            val id = content[0].digitToInt()
            val body = content.substring(2)
            val components = body.split("|")
            return when (id) {
                CURRENCY -> Currency(components[0], components.getOrElse(1) { "" })
                DATE, TIME, DATE_TIME -> components.toTemporal(id) ?: Plain(body)
                else -> Plain(body)
            }
        }

        private fun List<String>.toTemporal(id: Int): PassContent? {
            val date = getOrNull(0)?.let { TimeParser.parseOrNull(it) } ?: return null
            val format = getOrNull(1)?.toFormatStyleOrNull() ?: FormatStyle.MEDIUM
            val isRelative = getOrNull(2)?.toBoolean() ?: false
            return when (id) {
                DATE -> Date(date, format, isRelative)
                TIME -> Time(date, format, isRelative)
                else -> DateTime(date, format, isRelative)
            }
        }

        private fun String.toFormatStyleOrNull(): FormatStyle? =
            try {
                FormatStyle.valueOf(this)
            } catch (_: IllegalArgumentException) {
                null
            }
    }

    fun serialize(): String =
        when (this) {
            is Plain -> "$id|$text"
            is Currency -> "$id|$amount|$currency"
            is Date -> temporal(date, format, isRelative)
            is Time -> temporal(time, format, isRelative)
            is DateTime -> temporal(dateTime, format, isRelative)
        }

    private fun temporal(
        value: PassDateTime,
        format: FormatStyle,
        isRelative: Boolean,
    ): String = "$id|${value.serialize()}|${format.name}|$isRelative"

    abstract fun contains(query: String): Boolean

    abstract fun prettyPrint(): String

    abstract fun isEmpty(): Boolean

    fun isNotEmpty() = !isEmpty()
}

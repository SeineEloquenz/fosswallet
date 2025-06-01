package nz.eloque.foss_wallet.model.field

import nz.eloque.foss_wallet.utils.inIgnoreCase
import nz.eloque.foss_wallet.utils.prettyDate
import nz.eloque.foss_wallet.utils.prettyDateTime
import nz.eloque.foss_wallet.utils.prettyTime
import java.time.Instant
import java.time.format.FormatStyle

sealed class PassContent(val id: Int) {

    class Plain(val text: String) : PassContent(PLAIN) {
        override fun contains(query: String) = query inIgnoreCase text
        override fun prettyPrint(): String = text
    }

    class Currency(val amount: String, val currency: String) : PassContent(CURRENCY) {
        override fun contains(query: String) = query inIgnoreCase amount || query inIgnoreCase currency
        override fun prettyPrint(): String = amount + toCurrency(currency)

        private fun toCurrency(currencyCode: String): String? {
            return try {
                java.util.Currency.getInstance(currency).symbol
            } catch (_: IllegalArgumentException) {
                " $currencyCode"
            }
        }
    }

    data class Date(val date: String, val format: FormatStyle) : PassContent(DATE) {
        override fun contains(query: String) = query inIgnoreCase date
        override fun prettyPrint(): String  = Instant.parse(date).prettyDate(format)
    }

    data class Time(val time: String, val format: FormatStyle) : PassContent(TIME) {
        override fun contains(query: String) = query inIgnoreCase time
        override fun prettyPrint(): String  = Instant.parse(time).prettyTime(format)
    }

    data class DateTime(val dateTime: String, val format: FormatStyle) : PassContent(DATE_TIME) {
        override fun contains(query: String) = query inIgnoreCase dateTime
        override fun prettyPrint(): String  = Instant.parse(dateTime).prettyDateTime(format)
    }


    companion object {
        const val PLAIN = 0
        const val CURRENCY = 1
        const val DATE = 2
        const val TIME = 3
        const val DATE_TIME = 4

        fun deserialize(content: String): PassContent {
            return if (content[0].isDigit()) {
                val id = content[0].digitToInt()
                val content = content.substring(2)
                return when(id) {
                    CURRENCY -> content.split("|").let { Currency(it[0], it[1]) }
                    DATE -> content.split("|").let { Date(it[0], FormatStyle.valueOf(it[1])) }
                    TIME -> content.split("|").let { Time(it[0], FormatStyle.valueOf(it[1])) }
                    DATE_TIME -> content.split("|").let { DateTime(it[0], FormatStyle.valueOf(it[1])) }
                    else -> Plain(content)
                }
            } else {
                Plain(content)
            }
        }
    }

    fun serialize(): String {
        return when (this) {
            is Plain -> this.id.toString() + "|" + this.text
            is Currency -> this.id.toString() + "|" + this.amount + "|" + this.currency
            is Date -> this.id.toString() + "|" + this.date + "|" + this.format.name
            is Time -> this.id.toString() + "|" + this.time + "|" + this.format.name
            is DateTime -> this.id.toString() + "|" + this.dateTime + "|" + this.format.name
        }
    }

    abstract fun contains(query: String): Boolean
    abstract fun prettyPrint(): String
}
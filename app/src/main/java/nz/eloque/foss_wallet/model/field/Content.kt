package nz.eloque.foss_wallet.model.field

import androidx.room.Entity
import nz.eloque.foss_wallet.utils.inIgnoreCase
import nz.eloque.foss_wallet.utils.prettyPrint
import java.time.Instant
import java.time.format.FormatStyle

sealed class Content(val id: Int) {

    @Entity
    class Plain(val text: String) : Content(PLAIN) {
        override fun contains(query: String) = query inIgnoreCase text
        override fun prettyPrint(): String = text
    }

    @Entity
    class Currency(val amount: String, val currency: String) : Content(CURRENCY) {
        override fun contains(query: String) = query inIgnoreCase amount || query inIgnoreCase currency
        override fun prettyPrint(): String = amount + currency
    }

    @Entity
    data class Date(val date: String, val format: FormatStyle) : Content(DATE) {
        override fun contains(query: String) = query inIgnoreCase date
        override fun prettyPrint(): String  = Instant.parse(date).prettyPrint(format)
    }

    @Entity
    data class Time(val time: String, val format: FormatStyle) : Content(TIME) {
        override fun contains(query: String) = query inIgnoreCase time
        override fun prettyPrint(): String  = Instant.parse(time).prettyPrint(format)
    }


    companion object {
        const val PLAIN = 0
        const val CURRENCY = 1
        const val DATE = 2
        const val TIME = 3

        fun deserialize(content: String): Content {
            return if (content[0].isDigit()) {
                val id = content[0].digitToInt()
                val content = content.substring(2)
                return when(id) {
                    CURRENCY -> content.split("|").let { Currency(it[0], it[1]) }
                    DATE -> content.split("|").let { Date(it[0], FormatStyle.valueOf(it[1])) }
                    TIME -> content.split("|").let { Time(it[0], FormatStyle.valueOf(it[1])) }
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
            is Date -> this.id.toString() + "|" + this.date + this.format.name
            is Time -> this.id.toString() + "|" + this.time + this.format.name
        }
    }

    abstract fun contains(query: String): Boolean
    abstract fun prettyPrint(): String
}
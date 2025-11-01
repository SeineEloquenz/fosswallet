package nz.eloque.foss_wallet.parsing

import java.time.ZonedDateTime

object TimeParser {

    fun parse(value: String): ZonedDateTime {
        return ZonedDateTime.parse(value)
    }
}
package nz.eloque.foss_wallet.parsing

import java.time.Instant
import java.time.ZonedDateTime

object InstantParser {

    fun parse(value: String): Instant {
        return ZonedDateTime.parse(value).toInstant()
    }
}
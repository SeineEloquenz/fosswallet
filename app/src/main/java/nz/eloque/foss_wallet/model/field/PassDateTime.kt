package nz.eloque.foss_wallet.model.field

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * A date/time value as it appears in a pass field.
 *
 * The pkpass standard distinguishes two types of dates/times:
 *
 *  - An **absolute** point on the timeline. `ignoresTimeZone` is false (the default), so the value
 *    must be rendered in the *device's* time zone. A departure at `10:00-05:00` shows as `16:00`
 *    to a reader in Berlin.
 *  - A **floating** wall-clock reading. Either `ignoresTimeZone` is true, or the source string
 *    carried no offset at all. The value must be rendered exactly as written, in every time zone.
 */
sealed interface PassDateTime {
    /** A point on the timeline; displayed in the reader's time zone. */
    data class Absolute(
        val value: ZonedDateTime,
    ) : PassDateTime {
        override fun atZone(zone: ZoneId): LocalDateTime = value.withZoneSameInstant(zone).toLocalDateTime()

        override fun toInstant(zone: ZoneId): Instant = value.toInstant()

        override fun serialize(): String = value.toString()
    }

    /** A wall-clock reading with no zone; displayed as written, wherever it is read. */
    data class Floating(
        val value: LocalDateTime,
    ) : PassDateTime {
        override fun atZone(zone: ZoneId): LocalDateTime = value

        override fun toInstant(zone: ZoneId): Instant = value.atZone(zone).toInstant()

        override fun serialize(): String = value.toString()
    }

    /** The local date/time to render, given the zone the reader is in. */
    fun atZone(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime

    /**
     * The instant this value refers to. A [Floating] value only denotes an instant once a zone is
     * assumed, so [zone] is used to anchor it.
     */
    fun toInstant(zone: ZoneId = ZoneId.systemDefault()): Instant

    /**
     * The persisted representation. It is self-describing: an offset is present iff the value is
     * [Absolute], so no separate `ignoresTimeZone` flag needs storing alongside it.
     */
    fun serialize(): String

    companion object {
        /**
         * Reinterprets an absolute value as floating, discarding the offset but keeping the
         * wall-clock reading. This is what `ignoresTimeZone: true` asks for.
         */
        fun PassDateTime.ignoringTimeZone(): PassDateTime =
            when (this) {
                is Absolute -> Floating(value.toLocalDateTime())
                is Floating -> this
            }
    }
}

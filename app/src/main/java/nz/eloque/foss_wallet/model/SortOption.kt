package nz.eloque.foss_wallet.model

import androidx.annotation.StringRes
import nz.eloque.foss_wallet.R
import java.time.Instant
import java.time.ZonedDateTime

const val TIME_ADDED = "TimeAdded"
const val RELEVANT_DATE_NEWEST = "RelevantDateNewest" // This field has been renamed to Latest
const val RELEVANT_DATE_OLDEST = "RelevantDateOldest" // This field has been renamed to Earliest

private val timeAdded =
    Comparator.comparing<LocalizedPassWithTags, Instant?>(
        { it.pass.addedAt },
        Comparator.reverseOrder(),
    )

private val newestFirst =
    Comparator.comparing<LocalizedPassWithTags, ZonedDateTime?>(
        {
            it.pass.relevantDates
                .firstOrNull()
                ?.startDate()
        },
        Comparator.nullsLast(Comparator.reverseOrder()),
    )

private val oldestFirst =
    Comparator.comparing<LocalizedPassWithTags, ZonedDateTime?>(
        {
            it.pass.relevantDates
                .firstOrNull()
                ?.startDate()
        },
        Comparator.nullsLast(Comparator.naturalOrder()),
    )

sealed class SortOption(
    val name: String,
    @param:StringRes val l18n: Int,
    val comparator: Comparator<LocalizedPassWithTags>,
) {
    object TimeAdded : SortOption(TIME_ADDED, R.string.date_added, timeAdded)

    object RelevantDateLatest : SortOption(RELEVANT_DATE_NEWEST, R.string.relevant_date_latest, newestFirst)

    object RelevantDateEarliest : SortOption(RELEVANT_DATE_OLDEST, R.string.relevant_date_earliest, oldestFirst)

    companion object {
        fun all(): List<SortOption> = listOf(TimeAdded, RelevantDateLatest, RelevantDateEarliest)
    }
}

object SortOptionSerializer {
    fun serialize(sortOption: SortOption): String = sortOption.name

    fun deserialize(sortOption: String): SortOption? = SortOption.all().find { option -> option.name == sortOption }
}

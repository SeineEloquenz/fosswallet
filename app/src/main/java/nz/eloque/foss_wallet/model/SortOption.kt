package nz.eloque.foss_wallet.model

import androidx.annotation.StringRes
import androidx.compose.runtime.saveable.Saver
import nz.eloque.foss_wallet.R

const val TIME_ADDED = "TimeAdded"
const val RELEVANT_DATE_NEWEST = "RelevantDateNewest"
const val RELEVANT_DATE_OLDEST = "RelevantDateOldest"

sealed class SortOption(val name: String, @param:StringRes val l18n: Int, val comparator: Comparator<Pass>) {
    object TimeAdded : SortOption(TIME_ADDED, R.string.date_added, Comparator { left, right ->
        -left.addedAt.compareTo(right.addedAt)
    })
    object RelevantDateNewest : SortOption(RELEVANT_DATE_NEWEST, R.string.relevant_date_newest, compareByDescending { it.relevantDate ?: Long.MIN_VALUE })
    object RelevantDateOldest : SortOption(RELEVANT_DATE_OLDEST, R.string.relevant_date_oldest, compareBy { it.relevantDate ?: Long.MAX_VALUE })

    companion object {
        fun all(): List<SortOption> {
            return listOf(TimeAdded, RelevantDateNewest, RelevantDateOldest)
        }
    }
}

val SortOptionSaver: Saver<SortOption, String> = Saver(
    save = { it.name },
    restore = { SortOption.all().find { option -> option.name == it } }
)

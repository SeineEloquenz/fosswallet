package nz.eloque.foss_wallet.model

import androidx.room3.Embedded
import androidx.room3.Relation
import nz.eloque.foss_wallet.utils.toMapping

data class PassWithLocalization(
    @Embedded
    val pass: Pass,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["passId"],
    )
    val localizations: List<PassLocalization>,
) {
    fun applyLocalization(locale: String): Pass = pass.applyLocalization(localizations.toMapping(locale))
}

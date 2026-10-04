package nz.eloque.foss_wallet.model

import androidx.room.Embedded
import androidx.room.Relation
import nz.eloque.foss_wallet.utils.toMapping

data class PassWithLocalization(
    @Embedded
    val pass: Pass,
    @Relation(
        parentColumn = "id",
        entityColumn = "passId",
    )
    val localizations: List<PassLocalization>,
) {
    fun applyLocalization(locale: String): Pass = pass.applyLocalization(localizations.toMapping(locale))
}

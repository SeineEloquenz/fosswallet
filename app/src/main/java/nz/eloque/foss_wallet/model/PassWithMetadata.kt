package nz.eloque.foss_wallet.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import nz.eloque.foss_wallet.utils.toMapping

data class PassWithMetadata(
    @Embedded
    val pass: Pass,
    @Relation(
        parentColumn = "id",
        entityColumn = "passId",
    )
    val metadata: PassMetadata,
    @Relation(
        parentColumn = "id",
        entityColumn = "label",
        associateBy =
            Junction(
                value = PassTagCrossRef::class,
                parentColumn = "passId",
                entityColumn = "tagLabel",
            ),
    )
    val tags: List<Tag>,
    @Relation(
        parentColumn = "id",
        entityColumn = "passId",
    )
    val localizations: List<PassLocalization>,
    @Relation(
        parentColumn = "id",
        entityColumn = "passId",
    )
    val attachments: List<Attachment>,
) {
    fun applyLocalization(locale: String): LocalizedPassWithTags {
        val localizedPass = pass.applyLocalization(localizations.toMapping(locale))
        return LocalizedPassWithTags(localizedPass, metadata, tags.toSet(), attachments)
    }
}

package nz.eloque.foss_wallet.model

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import nz.eloque.foss_wallet.utils.toMapping

data class PassWithMetadata(
    @Embedded
    val pass: Pass,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["passId"],
    )
    val metadata: PassMetadata,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["label"],
        associateBy =
            Junction(
                value = PassTagCrossRef::class,
                parentColumns = ["passId"],
                entityColumns = ["tagLabel"],
            ),
    )
    val tags: List<Tag>,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["passId"],
    )
    val localizations: List<PassLocalization>,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["passId"],
    )
    val attachments: List<Attachment>,
) {
    fun applyLocalization(locale: String): LocalizedPassWithTags {
        val localizedPass = pass.applyLocalization(localizations.toMapping(locale))
        return LocalizedPassWithTags(localizedPass, metadata, tags.toSet(), attachments)
    }
}

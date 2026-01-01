package nz.eloque.foss_wallet.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField


private const val CHANGE_MESSAGE_FORMAT = "%@"

data class PassWithTagsAndLocalization(
    @Embedded
    val pass: Pass,

    @Relation(
        parentColumn = "id",
        entityColumn = "label",
        associateBy = Junction(
            value = PassTagCrossRef::class,
            parentColumn = "passId",
            entityColumn = "tagLabel"
        )
    )
    val tags: List<Tag>,

    @Relation(
        parentColumn = "id",
        entityColumn = "passId"
    )
    val localizations: List<PassLocalization>
) {
    fun applyLocalization(locale: String): LocalizedPassWithTags {
        val mapping = localeMapping(locale).ifEmpty { localeMapping("en") }
        val localizedPass = pass.copy(
            description = mapping[pass.description]?.text ?: pass.description,
            headerFields = pass.headerFields.applyLocalization(mapping),
            primaryFields = pass.primaryFields.applyLocalization(mapping),
            secondaryFields = pass.secondaryFields.applyLocalization(mapping),
            auxiliaryFields = pass.auxiliaryFields.applyLocalization(mapping),
            backFields = pass.backFields.applyLocalization(mapping),
        )

        return LocalizedPassWithTags(localizedPass, tags.toSet())
    }

    private fun List<PassField>.applyLocalization(mapping: Map<String, PassLocalization>): List<PassField> {
        return this.map { field ->

            val content = field.content.applyLocalization(mapping)

            val localizedLabel = mapping[field.label]?.text ?: field.label
            val localizedChangeMessage = (mapping[field.changeMessage]?.text ?: field.changeMessage) ?.replace(CHANGE_MESSAGE_FORMAT, content.prettyPrint())
            field.copy(label = localizedLabel, changeMessage = localizedChangeMessage, content = content)
        }
    }

    private fun PassContent.applyLocalization(mapping: Map<String, PassLocalization>): PassContent {
        return if (this is PassContent.Plain && mapping.containsKey(this.text)) {
            PassContent.Plain(mapping[this.text]!!.text)
        } else {
            this
        }
    }

    private fun localeMapping(locale: String): Map<String, PassLocalization> {
        return localizations.filter { it.lang == locale }.associateBy { it.label }
    }
}

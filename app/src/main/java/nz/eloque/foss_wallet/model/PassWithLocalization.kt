package nz.eloque.foss_wallet.model

import androidx.room.Embedded
import androidx.room.Relation

data class PassWithLocalization(
    @Embedded
    val pass: Pass,
    @Relation(
        parentColumn = "id",
        entityColumn = "passId"
    )
    val localizations: List<PassLocalization>
) {

    fun applyLocalization(locale: String): Pass {
        val mapping = localeMapping(locale).ifEmpty { localeMapping("en") }
        return pass.apply {
            description = mapping[description]?.text ?: description
            replaceFields(mapping, headerFields)
            replaceFields(mapping, primaryFields)
            replaceFields(mapping, secondaryFields)
            replaceFields(mapping, auxiliaryFields)
            replaceFields(mapping, backFields)
        }
    }

    private fun replaceFields(mapping: Map<String, PassLocalization>, fields: MutableList<PassField>) {
        fields.replaceAll { it.copy(label = mapping[it.label]?.text ?: it.label) }
    }

    private fun localeMapping(locale: String): Map<String, PassLocalization> {
        return localizations.filter { it.lang == locale }.associateBy { it.label }
    }
}
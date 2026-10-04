package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField

private const val CHANGE_MESSAGE_FORMAT = "%@"

/**
 * Returns a copy of this pass with every user-visible string replaced by its
 * localized counterpart from [mapping], leaving untranslated strings untouched.
 */
fun Pass.applyLocalization(mapping: Map<String, PassLocalization>): Pass =
    copy(
        description = mapping[description]?.text ?: description,
        headerFields = headerFields.applyLocalization(mapping),
        primaryFields = primaryFields.applyLocalization(mapping),
        secondaryFields = secondaryFields.applyLocalization(mapping),
        auxiliaryFields = auxiliaryFields.applyLocalization(mapping),
        backFields = backFields.applyLocalization(mapping),
        barCodes = barCodes.applyLocalization(mapping),
    )

private fun List<PassField>.applyLocalization(mapping: Map<String, PassLocalization>): List<PassField> =
    map { field ->
        val content = field.content.applyLocalization(mapping)
        val localizedChangeMessage =
            mapping.localize(field.changeMessage)?.replace(CHANGE_MESSAGE_FORMAT, content.prettyPrint())
        field.copy(
            label = mapping.localize(field.label),
            changeMessage = localizedChangeMessage,
            content = content,
        )
    }

private fun Set<BarCode>.applyLocalization(mapping: Map<String, PassLocalization>): Set<BarCode> =
    mapTo(LinkedHashSet(size)) { barCode ->
        barCode.copy(altText = mapping.localize(barCode.altText))
    }

private fun PassContent.applyLocalization(mapping: Map<String, PassLocalization>): PassContent =
    if (this is PassContent.Plain && mapping.containsKey(text)) {
        PassContent.Plain(mapping[text]!!.text)
    } else {
        this
    }

private fun Map<String, PassLocalization>.localize(value: String?): String? = value?.let { this[it]?.text ?: it }

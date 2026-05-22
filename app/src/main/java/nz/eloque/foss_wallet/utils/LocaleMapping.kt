package nz.eloque.foss_wallet.utils

import nz.eloque.foss_wallet.model.PassLocalization
import java.util.Locale

fun List<PassLocalization>.toMapping(locale: String): Map<String, PassLocalization> {
    val availableLanguageTags = this.map { it.lang() }.distinct()
    val preferredLanguages = listOf(locale, "en")
    val bestMatch =
        Locale.lookupTag(
            preferredLanguages.mapNotNull { it.toLanguageRangeOrNull() },
            availableLanguageTags,
        ) ?: "en"

    return this
        .filter { it.lang().equals(bestMatch, ignoreCase = true) }
        .associateBy { it.label }
}

private fun String.toLanguageRangeOrNull(): Locale.LanguageRange? = runCatching { Locale.LanguageRange(this) }.getOrNull()

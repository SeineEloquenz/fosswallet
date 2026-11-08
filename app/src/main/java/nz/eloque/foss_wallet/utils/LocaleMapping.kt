package nz.eloque.foss_wallet.utils

import nz.eloque.foss_wallet.model.PassLocalization
import java.util.Locale

fun List<PassLocalization>.toMapping(locale: String): Map<String, PassLocalization> {
    val availableLanguageTags = this.map { it.lang() }.distinct()
    val bestMatch = bestLanguage(availableLanguageTags, locale, "en") ?: "en"

    return this
        .filter { it.lang().equals(bestMatch, ignoreCase = true) }
        .associateBy { it.label }
}

fun bestLanguage(
    available: Collection<String>,
    vararg preferred: String,
): String? =
    Locale.lookupTag(preferred.mapNotNull { it.toLanguageRangeOrNull() }, available)?.let { tag ->
        available.first { it.equals(tag, ignoreCase = true) }
    }

private fun String.toLanguageRangeOrNull(): Locale.LanguageRange? = runCatching { Locale.LanguageRange(this) }.getOrNull()

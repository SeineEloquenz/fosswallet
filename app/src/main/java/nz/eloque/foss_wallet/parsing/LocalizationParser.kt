package nz.eloque.foss_wallet.parsing

import nz.eloque.foss_wallet.model.PassLocalization

object LocalizationParser {
    fun parseStrings(lang: String, content: String): Set<PassLocalization> {
        val withoutComments = content
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("//.*?$", RegexOption.MULTILINE), "")

        val localeRegex = Regex("\"(.*?)\"\\s*=\\s*\"((?:[^\"\\\\]|\\\\.)*?)\"\\s*;", RegexOption.DOT_MATCHES_ALL)
        val result = mutableSetOf<PassLocalization>()

        localeRegex.findAll(withoutComments).forEach { match ->
            val label = match.groupValues[1]
            val rawValue = match.groupValues[2]
            val text = unescapeString(rawValue)
            result.add(PassLocalization("", lang, label, text))
        }

        return result
    }

    private fun unescapeString(input: String): String {
        return input
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }
}
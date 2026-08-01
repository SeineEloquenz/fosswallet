package nz.eloque.foss_wallet.parsing

import com.dd.plist.ASCIIPropertyListParser
import com.dd.plist.NSDictionary
import nz.eloque.foss_wallet.model.PassLocalization

object LocalizationParser {
    fun parseStrings(
        lang: String,
        content: String,
    ): Set<PassLocalization> {
        val dict =
            ASCIIPropertyListParser.parse(
                // strings files only differ from plist files by them not being enclosed in curly braces
                """
                {
                $content
                }
                """.trimIndent(),
            ) as NSDictionary
        return dict
            .map { (key, value) ->
                PassLocalization("", lang, key, value.toString())
            }.toSet()
    }
}

package nz.eloque.foss_wallet.utils

fun linkifyUrls(text: String): String {
    val urlPattern = Regex(
        """(?<!["'>])https?://[^\s<>"']+(?<![\.,;:!?\)])""",
        RegexOption.IGNORE_CASE
    )
    
    return urlPattern.replace(text) { matchResult ->
        val url = matchResult.value
        """<a href="$url" data-linkified="true">$url</a>"""
    }
}

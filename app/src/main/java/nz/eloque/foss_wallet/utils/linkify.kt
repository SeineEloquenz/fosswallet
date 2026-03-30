package nz.eloque.foss_wallet.utils

fun linkify(text: String): String = linkifyUrls(linkifyMails(text))

fun linkifyUrls(text: String): String {
    val urlPattern = Regex(
        """(?<!["'>]|href=")(?:https?://|www\.)[^\s<>"'@]+(?<![.,;:!?)])(?![^<]*</a>)""",
        RegexOption.IGNORE_CASE
    )
    
    return urlPattern.replace(text) { matchResult ->
        val url = if (matchResult.value.startsWith("www.", ignoreCase = true)) "https://${matchResult.value}"
            else matchResult.value
        """<a href="$url" data-linkified="true">$url</a>"""
    }
}

fun linkifyMails(text: String): String {
    val mailPattern = Regex(
        """(?<!["'>]|href=")(?:mailto:)?[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}(?![^<]*</a>)""",
        RegexOption.IGNORE_CASE
    )

    return mailPattern.replace(text) { matchResult ->
        val mail = matchResult.value.removePrefix("mailto:")
        """<a href="mailto:$mail" data-linkified="true">$mail</a>"""
    }
}

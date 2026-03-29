package nz.eloque.foss_wallet.utils

fun linkify(text: String): String = linkifyUrls(linkifyMails(text))

fun linkifyUrls(text: String): String {
    val urlPattern = Regex(
        """(?<!["'>]|href=")(?:https?://|www\.)[^\s<>"'@]+(?<![.,;:!?)])(?![^<]*</a>)""",
        RegexOption.IGNORE_CASE
    )
    
    return urlPattern.replace(text) { matchResult ->
        val url = matchResult.value
        """<a href="$url" data-linkified="true" style="white-space:wrap">$url</a>"""
    }
}

fun linkifyMails(text: String): String {
    val mailPattern = Regex(
        """(?<!["'>]|href="|mailto:)[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}(?![^<]*</a>)""",
        RegexOption.IGNORE_CASE
    )

    return mailPattern.replace(text) { matchResult ->
        val mail = matchResult.value
        """<a href="mailto:$mail" data-linkified="true" style="white-space:wrap">$mail</a>"""
    }
}

package nz.eloque.foss_wallet.utils

import androidx.core.util.PatternsCompat

fun linkify(text: String): String = linkifyUrls(linkifyMails(text))

fun linkifyUrls(text: String): String {
    return PatternsCompat.WEB_URL.toRegex(RegexOption.IGNORE_CASE).replace(text) {
        if (it.value.contains("data-linkified")) return@replace it.value
        val href = if (!it.value.startsWith("http", ignoreCase = true)) "https://${it.value}" else it.value
        """<a href="$href" data-linkified="true">${it.value}</a>"""
    }
}

fun linkifyMails(text: String): String {
    return PatternsCompat.EMAIL_ADDRESS.toRegex(RegexOption.IGNORE_CASE).replace(text) {
        if (it.value.contains("data-linkified")) return@replace it.value
        val mail = it.value.removePrefix("mailto:")
        """<a href="mailto:$mail" data-linkified="true">$mail</a>"""
    }
}

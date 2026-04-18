package nz.eloque.foss_wallet.utils

import androidx.core.util.PatternsCompat

fun linkify(text: String): String {
    val tagPattern = Regex("<[^>]+>")
    val urlPattern = Regex(PatternsCompat.WEB_URL.pattern(), RegexOption.IGNORE_CASE)
    val mailPattern = Regex(PatternsCompat.EMAIL_ADDRESS.pattern(), RegexOption.IGNORE_CASE)
    val combined = Regex("${tagPattern.pattern}|${mailPattern.pattern}|${urlPattern.pattern}", RegexOption.IGNORE_CASE)

    return combined.replace(text) {
        when {
            tagPattern.matches(it.value) -> it.value
            mailPattern.matches(it.value) -> {
                val mail = it.value.removePrefix("mailto:")
                """<a href="mailto:$mail" data-linkified="true">$mail</a>"""
            }
            else -> {
                val href = if (!it.value.startsWith("http", ignoreCase = true)) {
                    "https://${it.value}" 
                } else {
                    it.value
                }
                """<a href="$href" data-linkified="true">${it.value}</a>"""
            }
        }
    }
}

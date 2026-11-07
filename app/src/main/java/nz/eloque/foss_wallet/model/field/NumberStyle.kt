package nz.eloque.foss_wallet.model.field

import android.icu.text.MessageFormat
import android.icu.text.NumberFormat
import android.icu.util.ULocale
import java.math.BigDecimal

enum class NumberStyle(
    val jsonValue: String,
) {
    DECIMAL("PKNumberStyleDecimal"),
    PERCENT("PKNumberStylePercent"),
    SCIENTIFIC("PKNumberStyleScientific"),
    SPELL_OUT("PKNumberStyleSpellOut"),
    ;

    fun format(
        number: BigDecimal,
        locale: ULocale,
    ): String =
        when (this) {
            DECIMAL -> NumberFormat.getInstance(locale, NumberFormat.NUMBERSTYLE).format(number)
            PERCENT -> NumberFormat.getInstance(locale, NumberFormat.PERCENTSTYLE).format(number)
            SCIENTIFIC -> NumberFormat.getInstance(locale, NumberFormat.SCIENTIFICSTYLE).format(number)
            SPELL_OUT -> MessageFormat("{0,spellout}", locale).format(arrayOf(number))
        }

    companion object {
        fun fromJson(value: String): NumberStyle? = entries.firstOrNull { it.jsonValue == value }
    }
}

package nz.eloque.foss_wallet.model.field

enum class TextAlignment(
    val jsonValue: String,
) {
    LEFT("PKTextAlignmentLeft"),
    CENTER("PKTextAlignmentCenter"),
    RIGHT("PKTextAlignmentRight"),
    NATURAL("PKTextAlignmentNatural"),
    ;

    companion object {
        fun fromJson(value: String): TextAlignment? = entries.firstOrNull { it.jsonValue == value }
    }
}

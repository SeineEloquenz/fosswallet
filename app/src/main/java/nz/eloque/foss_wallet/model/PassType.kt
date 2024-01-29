package nz.eloque.foss_wallet.model

enum class PassType {
    GENERIC,
    EVENT,
    COUPON,
    BOARDING,
    PKBOARDING,
    LOYALTY,
    VOUCHER
}


private val TYPE_TO_NAME = mapOf(
    PassType.COUPON to "coupon",
    PassType.EVENT to "eventTicket",
    PassType.PKBOARDING to "boardingPass",
    PassType.GENERIC to "generic",
    PassType.LOYALTY to "storeCard")

val TYPE_MAP = TYPE_TO_NAME.entries.associate { it.value to it.key }
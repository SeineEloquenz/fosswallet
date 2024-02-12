package nz.eloque.foss_wallet.model

enum class PassType(val jsonKey: String) {
    GENERIC("generic"),
    EVENT("eventTicket"),
    COUPON("coupon"),
    BOARDING("boardingPass"),
}
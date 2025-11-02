package nz.eloque.foss_wallet.model

import androidx.room.Entity

sealed class PassType(val jsonKey: String) {

    @Entity
    object Generic : PassType(GENERIC)
    @Entity
    object Event: PassType(EVENT)
    @Entity
    object Coupon : PassType(COUPON)
    @Entity
    data class Boarding(val transitType: TransitType) : PassType(BOARDING)
    @Entity
    object StoreCard : PassType(STORE_CARD)

    companion object {

        const val GENERIC = "generic"
        const val EVENT = "eventTicket"
        const val COUPON = "coupon"
        const val BOARDING = "boardingPass"
        const val STORE_CARD = "storeCard"
    }
}
package nz.eloque.foss_wallet.model

import androidx.room.Entity

sealed class PassType(val jsonKey: String) {

    @Entity
    class Generic : PassType(GENERIC)
    @Entity
    class Event: PassType(EVENT)
    @Entity
    class Coupon : PassType(COUPON)
    @Entity
    data class Boarding(val transitType: TransitType) : PassType(BOARDING)
    @Entity
    class StoreCard : PassType(STORE_CARD)

    companion object {

        const val GENERIC = "generic"
        const val EVENT = "eventTicket"
        const val COUPON = "coupon"
        const val BOARDING = "boardingPass"
        const val STORE_CARD = "storeCard"
    }
}
package nz.eloque.foss_wallet.model

import androidx.annotation.StringRes
import androidx.room.Entity
import nz.eloque.foss_wallet.R

sealed class PassType(val jsonKey: String, @param:StringRes val label:  Int) {

    @Entity
    object Generic : PassType(GENERIC, R.string.generic_pass)
    @Entity
    object Event: PassType(EVENT, R.string.event)
    @Entity
    object Coupon : PassType(COUPON, R.string.coupon)
    @Entity
    data class Boarding(val transitType: TransitType) : PassType(BOARDING, R.string.boarding_pass)
    @Entity
    object StoreCard : PassType(STORE_CARD, R.string.store_card)

    companion object {

        const val GENERIC = "generic"
        const val EVENT = "eventTicket"
        const val COUPON = "coupon"
        const val BOARDING = "boardingPass"
        const val STORE_CARD = "storeCard"
    }
}
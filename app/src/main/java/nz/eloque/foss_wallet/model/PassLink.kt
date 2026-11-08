package nz.eloque.foss_wallet.model

import android.net.Uri
import androidx.annotation.StringRes
import nz.eloque.foss_wallet.R

enum class PassLink(
    val key: String,
    val kind: Kind,
    @param:StringRes val label: Int,
) {
    MANAGEMENT("managementURL", Kind.WEB, R.string.link_management),
    TRANSFER("transferURL", Kind.WEB, R.string.link_transfer),
    SELL("sellURL", Kind.WEB, R.string.link_sell),
    UPGRADE("upgradeURL", Kind.WEB, R.string.link_upgrade),
    CHANGE_SEAT("changeSeatURL", Kind.WEB, R.string.link_change_seat),
    ADD_ON("addOnURL", Kind.WEB, R.string.link_add_on),
    ORDER_FOOD("orderFoodURL", Kind.WEB, R.string.link_order_food),
    MERCHANDISE("merchandiseURL", Kind.WEB, R.string.link_merchandise),
    ENTERTAINMENT("entertainmentURL", Kind.WEB, R.string.link_entertainment),
    PURCHASE_ADDITIONAL_BAGGAGE("purchaseAdditionalBaggageURL", Kind.WEB, R.string.link_purchase_additional_baggage),
    PURCHASE_LOUNGE_ACCESS("purchaseLoungeAccessURL", Kind.WEB, R.string.link_purchase_lounge_access),
    PURCHASE_WIFI("purchaseWifiURL", Kind.WEB, R.string.link_purchase_wifi),
    PURCHASE_PARKING("purchaseParkingURL", Kind.WEB, R.string.link_purchase_parking),
    TRACK_BAGS("trackBagsURL", Kind.WEB, R.string.link_track_bags),
    REPORT_LOST_BAG("reportLostBagURL", Kind.WEB, R.string.link_report_lost_bag),
    BAG_POLICY("bagPolicyURL", Kind.WEB, R.string.link_bag_policy),
    REQUEST_WHEELCHAIR("requestWheelchairURL", Kind.WEB, R.string.link_request_wheelchair),
    REGISTER_SERVICE_ANIMAL("registerServiceAnimalURL", Kind.WEB, R.string.link_register_service_animal),
    ACCESSIBILITY("accessibilityURL", Kind.WEB, R.string.link_accessibility),
    DIRECTIONS_INFORMATION("directionsInformationURL", Kind.WEB, R.string.link_directions_information),
    PARKING_INFORMATION("parkingInformationURL", Kind.WEB, R.string.link_parking_information),
    TRANSIT_INFORMATION("transitInformationURL", Kind.WEB, R.string.link_transit_information),
    CONTACT_VENUE_WEBSITE("contactVenueWebsite", Kind.WEB, R.string.link_contact_venue_website),
    CONTACT_VENUE_PHONE_NUMBER("contactVenuePhoneNumber", Kind.PHONE, R.string.link_contact_venue_phone_number),
    CONTACT_VENUE_EMAIL("contactVenueEmail", Kind.EMAIL, R.string.link_contact_venue_email),
    TRANSIT_PROVIDER_WEBSITE("transitProviderWebsiteURL", Kind.WEB, R.string.link_transit_provider_website),
    TRANSIT_PROVIDER_PHONE_NUMBER("transitProviderPhoneNumber", Kind.PHONE, R.string.link_transit_provider_phone_number),
    TRANSIT_PROVIDER_EMAIL("transitProviderEmail", Kind.EMAIL, R.string.link_transit_provider_email),
    ;

    enum class Kind { WEB, PHONE, EMAIL }

    /** The URI to open for [value], or null when a web link is not http or https. */
    fun uri(value: String): Uri? =
        when (kind) {
            Kind.WEB -> Uri.parse(value.trim()).takeIf { it.scheme?.lowercase() in WEB_SCHEMES }
            Kind.PHONE -> Uri.fromParts("tel", value.trim(), null)
            Kind.EMAIL -> Uri.fromParts("mailto", value.trim(), null)
        }

    companion object {
        private val WEB_SCHEMES = setOf("http", "https")

        /** The links of [links] that can be opened, in display order. */
        fun of(links: Map<String, String>): List<Pair<PassLink, Uri>> =
            entries.mapNotNull { link -> links[link.key]?.let(link::uri)?.let { link to it } }
    }
}

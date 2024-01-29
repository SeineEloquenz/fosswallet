package nz.eloque.foss_wallet.model

import android.location.Location
import java.time.ZonedDateTime


data class PassField(val key: String, private val label: String, private val value: String, val hide: Boolean, val hint:String = "")

class Pass(val id: String) {
    var creator: String = ""
    var type: PassType = PassType.EVENT
    var barCode: BarCode? = null
    var description: String? = null
        get() {
            if (field == null) {
                return "" // better way of returning no description - so we can avoid optional / null checks and it is kind of the same thing
                // an navigation_drawer_header description - we can do kind of all String operations safely this way and do not have to care about the existence of a real description
                // if we want to know if one is there we can check length for being 0 still ( which we would have to do anyway for navigation_drawer_header descriptions )
                // See no way at the moment where we would have to distinguish between an navigation_drawer_header and an missing description
            }
            return field
        }

    class TimeRepeat(val offset: Int, val count: Int)
    class TimeSpan(val from: ZonedDateTime? = null, val to: ZonedDateTime? = null, val repeat: TimeRepeat? = null)
    var validTimespans: List<TimeSpan> = ArrayList()
    var calendarTimespan: TimeSpan? = null
    var fields: MutableList<PassField> = ArrayList()
    var locations: List<Location> = ArrayList()
    var app: String? = null
    var authToken: String? = null
    var webServiceURL: String? = null
    var serial: String? = null
    var passIdent: String? = null
}
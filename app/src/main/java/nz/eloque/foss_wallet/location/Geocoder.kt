package nz.eloque.foss_wallet.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import okio.IOException
import java.util.Locale

class Geocoder @Inject constructor(@ApplicationContext context: Context) {

    private val geocoder = Geocoder(context, Locale.getDefault())
    private val cache: MutableMap<Location, Address?> = HashMap()

    fun decode(location: Location): Address? {
        return if (cache.contains(location)) {
            cache[location]
        } else {
            val result = try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            } catch (_: IOException) {
                null
            }
            cache[location] = result
            result
        }
    }
}
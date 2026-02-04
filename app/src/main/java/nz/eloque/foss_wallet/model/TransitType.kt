package nz.eloque.foss_wallet.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.ui.graphics.vector.ImageVector
import nz.eloque.foss_wallet.ui.icons.FlightTakeoff

private const val AIR_KEY = "PKTransitTypeAir"
private const val BOAT_KEY = "PKTransitTypeBoat"
private const val BUS_KEY = "PKTransitTypeBus"
private const val TRAIN_KEY = "PKTransitTypeTrain"
private const val GENERIC_KEY = "PKTransitTypeGeneric"

enum class TransitType(val jsonKey: String, val icon: ImageVector) {
    GENERIC(GENERIC_KEY, Icons.Default.KeyboardDoubleArrowRight),
    AIR(AIR_KEY, Icons.AutoMirrored.Default.FlightTakeoff),
    BOAT(BOAT_KEY, Icons.Default.DirectionsBoat),
    BUS(BUS_KEY, Icons.Default.DirectionsBus),
    TRAIN(TRAIN_KEY, Icons.Default.DirectionsTransit);

    companion object {

        fun fromName(name: String): TransitType  {
            return when (name) {
                AIR_KEY -> AIR
                BOAT_KEY -> BOAT
                BUS_KEY -> BUS
                TRAIN_KEY -> TRAIN
                else -> GENERIC
            }
        }
    }
}

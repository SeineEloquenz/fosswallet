package nz.eloque.foss_wallet.ui.components

import android.content.Intent
import android.location.Location
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri


@Composable
fun LocationButton(
    location: Location,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    IconButton(onClick = {
        context.startActivity(Intent(Intent.ACTION_VIEW).also {
            it.data = "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}".toUri()
        })
    }) {
        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location")
    }
}
package nz.eloque.foss_wallet.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R


@Composable
fun LocationButton(
    location: Location,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    IconButton(
        onClick = {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW).also {
                    it.data = "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}".toUri()
                })
            } catch (e: ActivityNotFoundException) {
                Log.e("LocationButton", "No map app found!", e)
                scope.launch {
                    snackbarHostState.showSnackbar(message = resources.getString(R.string.no_calendar_app_found))
                }
            }
        },
        modifier = modifier,
    ) {
        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location")
    }
}

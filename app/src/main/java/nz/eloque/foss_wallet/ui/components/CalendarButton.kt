package nz.eloque.foss_wallet.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Composable
fun CalendarButton(
    title: String,
    start: ZonedDateTime,
    end: ZonedDateTime?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    IconButton(onClick = {
        val intent = Intent(Intent.ACTION_EDIT).also {
            it.type = "vnd.android.cursor.item/event"
            it.putExtra("beginTime", start.toEpochSecond() * 1000)
            it.putExtra("allDay", false)
            it.putExtra("endTime", if (end != null) end.toEpochSecond() * 1000
            else start.plus(30, ChronoUnit.MINUTES).toEpochSecond() * 1000) //30 min default
            it.putExtra("title", title)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("DateView", "No calendar app found!", e)
            scope.launch {
                snackbarHostState.showSnackbar(message = resources.getString(R.string.no_calendar_app_found))
            }
        }

    }, modifier = modifier) {
        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = stringResource(R.string.date))
    }
}

package nz.eloque.foss_wallet.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
        }

    }, modifier = modifier) {
        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = stringResource(R.string.date))
    }
}
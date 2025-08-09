package nz.eloque.foss_wallet.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.utils.prettyDateTime
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun DateView(
    title: String,
    start: Long,
    end: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (start != 0L || end != 0L) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_EDIT).also {
                    it.type = "vnd.android.cursor.item/event"
                    it.putExtra("beginTime", Instant.ofEpochSecond(start).toEpochMilli())
                    it.putExtra("allDay", false)
                    it.putExtra("endTime", if (end != 0L) Instant.ofEpochSecond(end).toEpochMilli()
                        else Instant.ofEpochSecond(start).plus(30, ChronoUnit.MINUTES).toEpochMilli()) //30 min default
                    it.putExtra("title", title)
                }
                
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Log.e("DateView", "No calendar app found!", e)
                }
                
            }) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = stringResource(R.string.date))
            }
            Column {
                if (start != 0L) {
                    Text(
                        text = Instant.ofEpochSecond(start).prettyDateTime(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (end != 0L) {
                    Text(
                        text = Instant.ofEpochSecond(end).prettyDateTime(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DateViewStartOnlyPreview() {
    DateView(
        title = "KSC - HSV",
        start = 1000000000,
        end = 0
    )
}

@Preview
@Composable
private fun DateViewStartAndEndPreview() {
    DateView(
        title = "KSC - HSV",
        start = 1000000000,
        end = 1001000000
    )
}

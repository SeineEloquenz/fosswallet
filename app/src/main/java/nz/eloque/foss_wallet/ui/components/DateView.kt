package nz.eloque.foss_wallet.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.utils.prettyPrint
import java.time.Instant

@Composable
fun DateView(
    title: String,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_EDIT)
            intent.setType("vnd.android.cursor.item/event")
            intent.putExtra("beginTime", Instant.ofEpochSecond(timestamp).toEpochMilli())
            intent.putExtra("allDay", false)
            intent.putExtra("endTime", 3600000)
            intent.putExtra("title", title)
            context.startActivity(intent)
        }) {
            Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = stringResource(R.string.date))
        }
        Text(Instant.ofEpochSecond(timestamp).prettyPrint())
    }
}
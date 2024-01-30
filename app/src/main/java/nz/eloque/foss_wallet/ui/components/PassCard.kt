package nz.eloque.foss_wallet.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassCard(
    icon: Bitmap,
    description: String,
    date: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(5.dp)
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(5.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                bitmap = icon.asImageBitmap(),
                contentDescription = "logo",
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.CenterVertically)
                    .weight(2f)
            )
        }
    }
}

@Preview
@Composable
fun PassCardPreview() {
    PassCard(icon = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888), description = "KSC - HSV", date = "12.07.2023")
}
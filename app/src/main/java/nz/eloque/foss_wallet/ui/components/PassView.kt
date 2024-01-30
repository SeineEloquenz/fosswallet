package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.RawPass

@Composable
fun PassView(
    rawPass: RawPass,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(
                    text = rawPass.passJson.getString("description"),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(5.dp)
                )
                Text(
                    text = "placeholder",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(5.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                bitmap = rawPass.icon.asImageBitmap(),
                contentDescription = "logo",
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.CenterVertically)
                    .weight(2f)
            )
        }
        Text(rawPass.passJson.toString(2))
        Image(
            bitmap = rawPass.logo.asImageBitmap(),
            contentDescription = "logo",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}
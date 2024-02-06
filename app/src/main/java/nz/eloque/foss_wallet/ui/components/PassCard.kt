package nz.eloque.foss_wallet.ui.components

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassCard(
    iconModel: File,
    description: String,
    relevantDate: Long,
    expirationDate: Long,
    location: Location?,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(5f)
                )
                AsyncPassImage(
                    model = iconModel,
                    modifier = Modifier
                        .padding(5.dp)
                        .align(Alignment.CenterVertically)
                        .weight(2f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateView(description, relevantDate, expirationDate)
                location?.let { LocationButton(it) }
            }
        }
    }
}
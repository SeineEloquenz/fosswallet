package nz.eloque.foss_wallet.ui.components

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.ui.components.pass_view.AsyncPassImage
import nz.eloque.foss_wallet.ui.components.pass_view.HeaderFieldsView
import java.io.File

@Composable
fun PassCard(
    iconModel: File,
    description: String,
    headerFields: List<PassField>,
    relevantDate: Long,
    expirationDate: Long,
    location: Location?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    ElevatedCard(
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
                Column(
                    modifier = Modifier.weight(5f)
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    HeaderFieldsView(
                        headerFields = headerFields,
                        cardColors = CardDefaults.elevatedCardColors()
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                AsyncPassImage(
                    model = iconModel,
                    modifier = Modifier
                        .padding(5.dp)
                        .align(Alignment.CenterVertically)
                        .weight(2f)
                        .width(100.dp)
                        .height(100.dp)
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

@Preview
@Composable
private fun PasscardPreview() {
    PassCard(
        iconModel = File(""),
        description = "SV Elversberg",
        headerFields = listOf(
            PassField("Gate", "Gate", "37"),
            PassField("Group", "Group", "3"),
            PassField("Seat", "Seat", "47")
        ),
        relevantDate = 1000000000L,
        expirationDate = 0L,
        location = Location("")
    )
}
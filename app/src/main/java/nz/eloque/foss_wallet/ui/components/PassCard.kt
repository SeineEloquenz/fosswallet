package nz.eloque.foss_wallet.ui.components

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.PassField
import nz.eloque.foss_wallet.ui.components.pass_view.AsyncPassImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassCard(
    iconModel: File,
    description: String,
    headerFields: List<PassField>,
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
                Column(
                    modifier = Modifier.weight(5f)
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        for (field in headerFields) {
                            OutlinedTextField(
                                value = field.value,
                                label = { Text(field.label) },
                                readOnly = true,
                                onValueChange = {},
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
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
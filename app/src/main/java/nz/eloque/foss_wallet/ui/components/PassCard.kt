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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.PassField
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
                horizontalArrangement = Arrangement.Start
            ) {
                Column(
                    modifier = Modifier.weight(5f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = iconModel,
                            contentDescription = stringResource(R.string.image),
                            modifier = Modifier
                                .padding(5.dp)
                                .width(30.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = description,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    HeaderFieldsView(
                        headerFields = headerFields,
                        cardColors = CardDefaults.elevatedCardColors()
                    )
                }
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
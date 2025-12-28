package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nz.eloque.foss_wallet.model.Tag

@Composable
fun TagChooser(
    tags: Set<Tag>,
    onSelected: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier,
    ) {
        tags.forEach { tag ->
            ElevatedButton(onClick = {
                onSelected(tag)
            }) {
                Text(
                    text = tag.label,
                )
            }
        }
    }
}
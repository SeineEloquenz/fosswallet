package nz.eloque.foss_wallet.ui.components.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.Tag

@Composable
fun TagChooser(
    tags: Set<Tag>,
    onSelected: (Tag) -> Unit,
    onTagCreate: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        tags.forEach { tag ->
            item {
                Button(onClick = {
                    onSelected(tag)
                }) {
                    Text(
                        text = tag.label,
                    )
                }
            }
        }

        item { HorizontalDivider() }

        item {
            TagCreator(
                onCreate = onTagCreate,
            )
        }
    }
}
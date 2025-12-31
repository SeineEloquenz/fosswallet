package nz.eloque.foss_wallet.ui.components.tag

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Tag

@Composable
fun TagChooser(
    tags: Set<Tag>,
    onSelected: (Tag) -> Unit,
    onTagCreate: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    var creatorShown by remember { mutableStateOf(false) }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        tags.forEach { tag ->
            item {
                Button(
                    onClick = {
                        onSelected(tag)
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = tag.color,
                        contentColor = tag.contentColor()
                    )
                ) {
                    Text(
                        text = tag.label,
                    )
                }
            }
        }

        item { HorizontalDivider() }

        item {
            Button(onClick = {
                creatorShown = true
            }) {
                Text(stringResource(R.string.add_tag))
            }
        }
    }

    if (creatorShown) {
        Dialog(
            onDismissRequest = { creatorShown = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                TagCreator(
                    onCreate = {
                        onTagCreate(it)
                        creatorShown = false
                    },
                    modifier = Modifier.padding(20.dp)
                )
            }

        }
    }
}
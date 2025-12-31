package nz.eloque.foss_wallet.ui.components.tag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.ui.components.ChipSelector
import nz.eloque.foss_wallet.ui.screens.wallet.PassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagRow(
    tags: Set<Tag>,
    selectedTag: Tag?,
    onTagSelected: (Tag) -> Unit,
    onTagDeselected: (Tag) -> Unit,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var tagCreatorShown by remember { mutableStateOf(false) }
    var tagRemoverShown by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        ChipSelector(
            options = tags,
            selectedOptions = listOfNotNull(selectedTag),
            onOptionSelected = { onTagSelected(it) },
            onOptionDeselected = {
                if (selectedTag == it) {
                    onTagDeselected(it)
                }
            },
            optionLabel = { it.label },
            modifier = Modifier.weight(1f),
            selectedIcon = Icons.Default.FilterAlt,
        )
        IconButton(
            onClick = {
                tagCreatorShown = true
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_tag)
            )
        }
        IconButton(
            onClick = {
                tagRemoverShown = true
            }
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = stringResource(R.string.remove_tag)
            )
        }
    }

    if (tagCreatorShown) {
        ModalBottomSheet(
            onDismissRequest = {
                tagCreatorShown = false
            },
            modifier = Modifier.fillMaxWidth()) {
            TagCreator(
                onCreate = {
                    tagCreatorShown = false
                    coroutineScope.launch(Dispatchers.IO) { passViewModel.addTag(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (tagRemoverShown) {
        ModalBottomSheet(
            onDismissRequest = {
                tagRemoverShown = false
            },
            modifier = Modifier.fillMaxWidth()) {
            TagChooser(
                tags = tags,
                onSelected = { coroutineScope.launch(Dispatchers.IO) { passViewModel.removeTag(it) }},
                onTagCreate = { coroutineScope.launch(Dispatchers.IO) { passViewModel.addTag(it) }},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
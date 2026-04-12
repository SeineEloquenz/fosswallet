package nz.eloque.foss_wallet.ui.screens.wallet

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T, F> SelectionMenu(
    multiOptions: Collection<F>,
    singleOptions: List<T>,
    multiOptionLabel: (T) -> String,
    singleOptionLabel: (F) -> String,
    selectedMultiOptions: Collection<F>,
    selectedSingleOption: T,
    onMultiOptionSelected: (F) -> Unit,
    onSingleOptionSelected: (T) -> Unit,
    onSingleOptionDeselected: (F) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes contentDescription: Int = R.string.more_options,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.Menu, contentDescription = stringResource(contentDescription))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            multiOptions.forEach { option -> // multi select
                val selected = selectedMultiOptions.contains(option)
                DropdownMenuItem(
                    text = { Text(multiOptionLabel(option)) },
                    leadingIcon = {
                        if (selected) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected))
                        }
                    },
                    onClick = { if (selected) onMultiOptionDeselected(option) else onMultiOptionSelected(option) }
                )
            }

            HorizontalDivider()

            singleOptions.forEach { option -> // single select
                DropdownMenuItem(
                    text = { Text(singleOptionLabel(option)) },
                    leadingIcon = {
                        if (option == selectedSingleOption) {
                            Icon(Icons.Default.RadioButtonChecked, stringResource(R.string.selected))
                        }
                    },
                    onClick = { onSingleOptionSelected(option) }
                )
            }
        }
    }
}

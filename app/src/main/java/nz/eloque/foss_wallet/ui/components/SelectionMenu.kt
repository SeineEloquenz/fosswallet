package nz.eloque.foss_wallet.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectionMenu(
    options: List<T>,
    selectedOption: T,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Menu,
    @StringRes contentDescription: Int = R.string.more_options,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String
) {
    val expanded = remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        IconButton(onClick = { expanded.value = !expanded.value }) {
            Icon(icon, contentDescription = stringResource(contentDescription))
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    trailingIcon = { if (option == selectedOption) {
                        Icon(Icons.Default.Check, stringResource(R.string.selected))
                    } },
                    onClick = { onOptionSelected(option) }
                )
            }
        }
    }

}

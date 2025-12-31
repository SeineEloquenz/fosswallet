package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R

@Composable
fun <T> ChipSelector(
    options: Collection<T>,
    selectedOptions: Collection<T>,
    onOptionSelected: (T) -> Unit,
    onOptionDeselected: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    selectedIcon: ImageVector = Icons.Default.Check,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
    ) {
        options.forEach { option ->
            val selected = selectedOptions.contains(option)
            FilterChip(
                selected = selected,
                leadingIcon = {
                    if (selected) {
                        Icon(
                            imageVector = selectedIcon,
                            contentDescription = stringResource(R.string.selected)
                        )
                    }
                },
                onClick = {
                    if (selected) {
                        onOptionDeselected(option)
                    } else {
                        onOptionSelected(option)
                    }
                },
                label = { Text(optionLabel(option)) },
            )
        }
    }
}
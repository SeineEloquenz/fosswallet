package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> ChipRow(
    options: Collection<T>,
    onOptionClick: (T) -> Unit,
    optionLabel: (T) -> String,
    optionColors: (T) -> SelectableChipColors,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (T) -> Unit = {},
    trailingIcon: @Composable (T) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
    ) {
        options.forEach { option ->
            FilterChip(
                selected = false,
                leadingIcon = { leadingIcon(option) },
                trailingIcon = { trailingIcon(option) },
                onClick = { onOptionClick(option) },
                label = { Text(optionLabel(option)) },
                colors = optionColors(option)
            )
        }
    }
}
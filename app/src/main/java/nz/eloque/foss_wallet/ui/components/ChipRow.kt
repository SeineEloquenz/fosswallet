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
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .horizontalScroll(rememberScrollState())
    ) {
        options.forEach { option ->
            FilterChip(
                selected = false,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                onClick = { onOptionClick(option) },
                label = { Text(optionLabel(option)) },
                colors = optionColors(option)
            )
        }
    }
}
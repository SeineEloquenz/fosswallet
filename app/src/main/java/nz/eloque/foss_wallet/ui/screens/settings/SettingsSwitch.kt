package nz.eloque.foss_wallet.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SettingsSwitch(
    @StringRes name: Int,
    switchState: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = name),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = switchState,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}
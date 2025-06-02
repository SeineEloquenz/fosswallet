package nz.eloque.foss_wallet.ui.components.pass_view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TurnLeft
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R

@Composable
fun PassViewBottomBar(
    front: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier,
    ) {
        NavigationBarItem(
            selected = front.value,
            onClick = {
                if (!front.value) {
                    front.value = !front.value
                }
            },
            icon = { Icon(imageVector = Icons.Default.TurnLeft, contentDescription = stringResource(R.string.front_side)) },
            label = { Text(stringResource(R.string.front_side)) },
        )
        NavigationBarItem(
            selected = !front.value,
            onClick = {
                front.value = !front.value
                if (front.value) {
                    front.value = !front.value
                }
            },
            icon = { Icon(imageVector = Icons.Default.TurnRight, contentDescription = stringResource(R.string.back_side)) },
            label = { Text(stringResource(R.string.back_side)) },
        )
    }
}
package nz.eloque.foss_wallet.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

data class FabMenuItem(
    val icon: ImageVector,
    val title: String,
    val onClick: (() -> Unit),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu(
    items: List<FabMenuItem>
) {
    Box {
        var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
        BackHandler(fabMenuExpanded) { fabMenuExpanded = false }
        FloatingActionButtonMenu(
            modifier = Modifier.align(Alignment.BottomEnd),
            expanded = fabMenuExpanded,
            button = {
                ToggleFloatingActionButton(
                    checked = fabMenuExpanded,
                    onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                ) {
                    val imageVector by remember {
                        derivedStateOf {
                            if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                        }
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress }),
                    )
                }
            },
        ) {
            items.forEachIndexed { i, item ->
                FloatingActionButtonMenuItem(
                    onClick = {
                        fabMenuExpanded = false
                        item.onClick()
                    },
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    text = { Text(text = item.title) },
                )
            }
        }
    }

}
package nz.eloque.foss_wallet.ui.screens.pass

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.shortcut.Shortcut
import nz.eloque.foss_wallet.widget.PassCardGlanceWidgetReceiver

/**
 * Replaces the previous plain "Add shortcut" DropdownMenuItem. If the launcher supports
 * AppWidgetManager.requestPinAppWidget (Android 8+, launcher-dependent), tapping the icon opens
 * a bottom sheet to choose between a shortcut and a widget. Otherwise it falls back to the
 * original direct "create shortcut" behaviour, since there'd only be one option anyway.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToHomeScreenMenuItem(
    pass: Pass,
    settingsStore: SettingsStore,
) {
    val context = LocalContext.current
    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
    val widgetPinSupported = appWidgetManager?.isRequestPinAppWidgetSupported == true

    var chooserShown by remember { mutableStateOf(false) }

    DropdownMenuItem(
        text = { Text(stringResource(R.string.add_to_home_screen)) },
        leadingIcon = {
            Icon(imageVector = Icons.Default.AppShortcut, contentDescription = stringResource(R.string.add_to_home_screen))
        },
        onClick = {
            if (widgetPinSupported) {
                chooserShown = true
            } else {
                Shortcut.create(context, pass, pass.description)
            }
        },
    )

    if (chooserShown) {
        ModalBottomSheet(onDismissRequest = { chooserShown = false }) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.add_shortcut)) },
                leadingContent = { Icon(imageVector = Icons.Default.AppShortcut, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        chooserShown = false
                        Shortcut.create(context, pass, pass.description)
                    },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.add_widget)) },
                leadingContent = { Icon(imageVector = Icons.Default.Widgets, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        chooserShown = false
                        settingsStore.setPendingWidgetPassId(pass.id)
                        appWidgetManager?.requestPinAppWidget(
                            ComponentName(context, PassCardGlanceWidgetReceiver::class.java),
                            null,
                            null,
                        )
                    },
            )
        }
    }
}

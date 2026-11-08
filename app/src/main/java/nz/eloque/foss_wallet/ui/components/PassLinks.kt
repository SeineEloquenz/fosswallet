package nz.eloque.foss_wallet.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import nz.eloque.compose_kit.components.Section
import nz.eloque.compose_kit.scaffold.LocalSnackbarHostState
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.PassLink

@Composable
fun PassLinks(links: List<Pair<PassLink, Uri>>) {
    if (links.isEmpty()) return
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current

    Section(stringResource(R.string.links)) {
        links.forEach { (link, uri) ->
            ListItem(
                headlineContent = { Text(stringResource(link.label)) },
                leadingContent = { Icon(link.kind.icon(), contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier =
                    Modifier.clickable {
                        try {
                            context.startActivity(link.kind.intent(uri))
                        } catch (e: ActivityNotFoundException) {
                            Log.e("PassLinks", "No app found for ${link.key}", e)
                            scope.launch { snackbarHostState.showSnackbar(resources.getString(R.string.no_app_for_link)) }
                        }
                    },
            )
        }
    }
}

private fun PassLink.Kind.icon() =
    when (this) {
        PassLink.Kind.WEB -> Icons.Default.Language
        PassLink.Kind.PHONE -> Icons.Default.Phone
        PassLink.Kind.EMAIL -> Icons.Default.Email
    }

private fun PassLink.Kind.intent(uri: Uri) =
    when (this) {
        PassLink.Kind.WEB -> Intent(Intent.ACTION_VIEW, uri)
        PassLink.Kind.PHONE -> Intent(Intent.ACTION_DIAL, uri)
        PassLink.Kind.EMAIL -> Intent(Intent.ACTION_SENDTO, uri)
    }

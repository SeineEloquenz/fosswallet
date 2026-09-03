package nz.eloque.foss_wallet.ui.screens.about

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import nz.eloque.compose_kit.components.About
import nz.eloque.compose_kit.components.AboutLink
import nz.eloque.foss_wallet.BuildConfig
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.Screen

@Composable
fun AboutView(navController: NavHostController) {
    About(
        appName = stringResource(R.string.app_name),
        icon = painterResource(R.drawable.icon),
        tagline = stringResource(R.string.made_with_love),
        taglineIcon = Icons.Default.Construction,
        version = "v${BuildConfig.VERSION_NAME}-${BuildConfig.BUILD_TYPE}-${BuildConfig.VERSION_CODE}",
        links =
            listOf(
                AboutLink.Uri(
                    icon = Icons.Default.Source,
                    label = stringResource(R.string.source_code),
                    url = "https://github.com/SeineEloquenz/fosswallet",
                ),
                AboutLink.Uri(
                    icon = Icons.Default.Balance,
                    label = stringResource(R.string.license),
                    url = "https://github.com/SeineEloquenz/fosswallet/blob/main/LICENSE",
                ),
                AboutLink.Uri(
                    icon = Icons.Default.PrivacyTip,
                    label = stringResource(R.string.privacy),
                    url = "https://github.com/SeineEloquenz/fosswallet/blob/main/PRIVACY.md",
                ),
                AboutLink.Uri(
                    icon = Icons.Default.Translate,
                    label = stringResource(R.string.help_translate),
                    url = "https://hosted.weblate.org/projects/fosswallet/",
                ),
                AboutLink.Action(
                    icon = Screen.Libraries.icon,
                    label = stringResource(Screen.Libraries.resourceId),
                    onClick = { navController.navigate(Screen.Libraries.route) },
                ),
            ),
    )
}

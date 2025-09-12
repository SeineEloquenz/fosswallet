package nz.eloque.foss_wallet.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import nz.eloque.foss_wallet.BuildConfig
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.Screen

@Composable
fun AboutView(
    navController: NavHostController,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.icon),
                contentDescription = stringResource(R.string.wallet),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            Text(
                text = stringResource(R.string.app_name),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall
            )
            AboutContent(
                icon = Icons.Default.Construction,
                text = stringResource(R.string.made_with_love),
                textStyle = MaterialTheme.typography.labelLarge
            )
        }
        UriButton(
            icon = Icons.Default.Source,
            text = stringResource(R.string.source_code),
            uri = "https://github.com/SeineEloquenz/fosswallet"
        )
        UriButton(
            icon = Icons.Default.Balance,
            text = stringResource(R.string.license),
            uri = "https://github.com/SeineEloquenz/fosswallet/blob/main/LICENSE"
        )
        UriButton(
            icon = Icons.Default.PrivacyTip,
            text = stringResource(R.string.privacy),
            uri = "https://github.com/SeineEloquenz/fosswallet/blob/main/PRIVACY.md"
        )
        LicensesButton(navController)
    }
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = "v${BuildConfig.VERSION_NAME}-${BuildConfig.BUILD_TYPE}-${BuildConfig.VERSION_CODE}",
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun UriButton(
    icon: ImageVector,
    text: String,
    uri: String,
) {
    val uriHandler = LocalUriHandler.current
    OutlinedButton(
        onClick = { uriHandler.openUri(uri) },
        modifier = Modifier.fillMaxWidth().padding(50.dp, 0.dp)
    ) {
        AboutContent(icon = icon, text = text)
    }
}

@Composable
private fun LicensesButton(
    navController: NavHostController
) {
    OutlinedButton(
        onClick = { navController.navigate(Screen.Libraries.route) },
        modifier = Modifier.fillMaxWidth().padding(50.dp, 0.dp)
    ) {
        AboutContent(icon = Screen.Libraries.icon, text = stringResource(Screen.Libraries.resourceId))
    }
}


@Composable
fun AboutContent(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            style = textStyle,
            textAlign = TextAlign.Center
        )
    }
}

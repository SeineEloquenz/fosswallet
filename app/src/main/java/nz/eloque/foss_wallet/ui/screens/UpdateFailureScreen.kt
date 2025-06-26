package nz.eloque.foss_wallet.ui.screens

import android.content.ClipData
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateFailureScreen(
    reason: String,
    rationale: String,
    navController: NavHostController,
) {
    val clipboard = LocalClipboard.current
    WalletScaffold(
        navController = navController,
        toolWindow = true,
        title = stringResource(R.string.details),
        actions = {
            IconButton(onClick = {
                clipboard.nativeClipboard.setPrimaryClip(ClipData.newPlainText(reason, rationale))
            }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = reason,
                modifier = Modifier.fillMaxWidth(),
                style = Typography.headlineMedium
            )
            Text(
                text = rationale,
                modifier = Modifier.fillMaxWidth(),
                style = Typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
private fun UpdateFailureScreenPreview() {
    UpdateFailureScreen(
        "Exception",
        """
            This is a stacktrace
            
            d
            w
            d
            d
            ad
            wa
            d
            wad
            
            
            
            d
            wa
            dwa
            dw
            ad
            wa
            dwa
            d
            a
            wd
            w
            ad
            wa
            d
            awd
            
            wad
            wa
            
            d
            dwadwadwa
            dwa
            dwa
            d
            wad
            wad
            wa
            dwa
            d
            awdw
        """.trimIndent(),
        rememberNavController()
    )
}
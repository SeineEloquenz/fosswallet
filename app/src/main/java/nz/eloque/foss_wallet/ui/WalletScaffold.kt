package nz.eloque.foss_wallet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.components.AbbreviatingText


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.wallet),
    toolWindow: Boolean = false,
    showBack: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (scrollBehavior: TopAppBarScrollBehavior) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { AbbreviatingText(
                    title,
                    maxLines = 1,
                ) },
                navigationIcon = {
                    if (toolWindow && showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    } else { Image(painter = painterResource(R.drawable.icon), contentDescription = null) }
                },
                actions = actions,
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = bottomBar,
        floatingActionButton = {
            Box(
                modifier = Modifier.padding(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
            ) {
                floatingActionButton()
            }
        }
    ) { innerPadding ->
        Box(modifier = modifier
            .padding(innerPadding)
            .padding(horizontal = 8.dp)
        ) {
            content.invoke(scrollBehavior)
        }
    }
}

package nz.eloque.foss_wallet.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.eloque.compose_kit.input.AbbreviatingText
import nz.eloque.compose_kit.scaffold.AppScaffold
import nz.eloque.foss_wallet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScaffold(
    navController: NavController,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.wallet),
    toolWindow: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (scrollBehavior: TopAppBarScrollBehavior) -> Unit,
) {
    AppScaffold(
        title = {
            AbbreviatingText(
                title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (toolWindow) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            }
        },
        actions = actions,
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        snackbarHostState = snackbarHostState,
        contentHorizontalPadding = 8.dp,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScaffoldWithFilterBar(
    navController: NavController,
    imageVector: ImageVector,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    toolWindow: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    subRow: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable(scrollBehavior: TopAppBarScrollBehavior) -> Unit,
) {
    AppScaffold(
        title = {
            FilterBar(
                imageVector = imageVector,
                modifier = Modifier.fillMaxWidth(),
                onSearch = onSearch,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (toolWindow) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            }
        },
        actions = actions,
        floatingActionButton = floatingActionButton,
        bottomBar = bottomBar,
        snackbarHostState = snackbarHostState,
        contentHorizontalPadding = 8.dp,
        subRow = subRow,
        content = content,
    )
}

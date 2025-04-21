package nz.eloque.foss_wallet.ui.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.ui.components.PassCard

@Composable
fun WalletView(
    navController: NavController,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current
    val list = passViewModel.uiState.collectAsState()

    val comparator by remember { mutableStateOf( Comparator<Pass> { left, right ->
        left.addedAt.compareTo(right.addedAt)
    }) }

    if (list.value.passes.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = Icons.Default.Wallet,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                contentDescription = stringResource(R.string.wallet),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(0.5f),
                alpha = 0.25f
            )
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement
            .spacedBy(10.dp),
        modifier = modifier
            .fillMaxSize()
    ) {
        val sortedPasses = list.value.passes.sortedWith(comparator)
        items(sortedPasses, { pass: Pass -> pass.id }) { pass ->
            PassCard(
                pass = pass,
                onClick = {
                    navController.navigate("pass/${pass.id}")
                },
            ) {

            }
        }
    }
}
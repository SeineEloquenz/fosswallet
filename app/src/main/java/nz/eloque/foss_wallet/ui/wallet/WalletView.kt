package nz.eloque.foss_wallet.ui.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
        -left.id.compareTo(right.id)
    }) }
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
                onClick = {
                    navController.navigate("pass/${pass.id}")
                },
                iconModel = pass.thumbnailFile(context) ?: pass.iconFile(context),
                description = pass.description,
                headerFields = pass.headerFields,
                relevantDate = pass.relevantDate,
                expirationDate = pass.expirationDate,
                location = pass.locations.firstOrNull()
            )
        }
    }
}
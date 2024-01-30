package nz.eloque.foss_wallet.ui.wallet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.persistence.PassLoader
import nz.eloque.foss_wallet.ui.components.PassCard
import nz.eloque.foss_wallet.ui.components.SwipeBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletView(
    navController: NavController,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val state = rememberLazyListState()
    val list = passViewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { res ->
        res?.let {
            println("selected file URI $res")
            contentResolver.openInputStream(res!!)?.use { inputStream ->
                val loaded = PassLoader(context).load(inputStream)
                coroutineScope.launch(Dispatchers.IO) { passViewModel.add(loaded) }
            }
        }
    }
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            state = state,
            verticalArrangement = Arrangement
                .spacedBy(10.dp),
            modifier = modifier
                .fillMaxWidth()
                .weight(9f)
        ) {
            items(list.value.passes) { pass ->
                val currentPass by rememberUpdatedState(pass)
                val dismissState = rememberDismissState(
                    positionalThreshold = { 150.dp.toPx() },
                    confirmValueChange = {
                        if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                            coroutineScope.launch(Dispatchers.IO) { passViewModel.delete(currentPass) }
                            true
                        } else {
                            false
                        }
                    }
                )
                SwipeToDismiss(
                    state = dismissState,
                    background = { SwipeBackground(dismissState, Icons.Filled.Delete, Icons.Filled.Delete) },
                    dismissContent = {
                        PassCard(
                            onClick = {
                                navController.navigate("pass/${pass.id}")
                            },
                            icon = pass.thumbnail ?: pass.icon,
                            description = pass.description,
                            relevantDate = pass.relevantDate
                        )
                    }
                )
            }
        }
        FloatingActionButton(
            onClick = { launcher.launch(arrayOf("*/*")) },
            modifier = Modifier
                .align(Alignment.End)
                .weight(1f)
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add pass")
        }
    }
}
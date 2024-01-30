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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import nz.eloque.foss_wallet.model.PassLoader
import nz.eloque.foss_wallet.model.PassStore
import nz.eloque.foss_wallet.model.RawPass
import nz.eloque.foss_wallet.ui.components.PassCard

@Composable
fun WalletView(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val contentResolver = LocalContext.current.contentResolver

    val _passes = remember { MutableStateFlow(listOf<String>()) }
    val passes by remember { _passes }.collectAsState()
    val state = rememberLazyListState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        println("selected file URI $it")
        contentResolver.openInputStream(it!!)?.use { inputStream ->
            val loaded = PassLoader.load(inputStream)
            val id = PassStore.add(loaded)
            _passes.update { it + id }

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
            items(passes) { passId ->
                val pass = PassStore.get(passId)
                PassCard(
                    onClick = {
                        navController.navigate("pass/${passId}")
                    },
                    icon = pass.icon,
                    description = pass.passJson.getString("description"),
                    date = "placeholder"
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
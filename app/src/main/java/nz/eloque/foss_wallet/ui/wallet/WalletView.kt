package nz.eloque.foss_wallet.ui.wallet

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import nz.eloque.foss_wallet.model.PassLoader
import nz.eloque.foss_wallet.model.RawPass

@Composable
fun WalletView(
    modifier: Modifier = Modifier
) {
    val contentResolver = LocalContext.current.contentResolver

    val _passes = remember { MutableStateFlow(listOf<RawPass>()) }
    val passes by remember { _passes }.collectAsState()
    val state = rememberLazyListState()

    val result = remember { mutableStateOf<RawPass?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        println("selected file URI $it")
        contentResolver.openInputStream(it!!)?.use { inputStream ->
            result.value = PassLoader.load(inputStream)
        }
    }
    Column(
        modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            state = state,
            modifier = modifier
                .fillMaxWidth()
                .weight(9f)
        ) {
            items(passes) { pass ->
                Card {
                    Text(pass.passJson.toString())
                }
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
        result.value?.let {
            Image(bitmap = it.logo.asImageBitmap(), contentDescription = "")
        }
    }
}
package nz.eloque.foss_wallet.ui.screens.create

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedAddScreen(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    WalletScaffold(
        navController = navController,
        toolWindow = true,
        title = stringResource(R.string.advanced)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val entry = clipboard.getClipEntry()

                        if (entry == null) {
                            Toast.makeText(context, resources.getString(R.string.no_text_in_clipboard), Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        var clipboardValue: String? = null
                        for (i in 0 until entry.clipData.itemCount) {
                            val item = entry.clipData.getItemAt(i)
                            val value = item?.coerceToText(context)?.toString().orEmpty()
                            if (value.isNotEmpty()) {
                                clipboardValue = value
                                break
                            }
                        }

                        val value = clipboardValue
                        if (value == null) {
                            Toast.makeText(context, resources.getString(R.string.no_text_in_clipboard), Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        withContext(Dispatchers.Main) {
                            if (value.startsWith("https://") || value.startsWith("http://")) {
                                navController.navigate("${Screen.Web.route}/${URLEncoder.encode(value, Charsets.UTF_8.name())}")
                            } else {
                                navController.navigate("${Screen.Create.route}?barcode=${URLEncoder.encode(value, Charsets.UTF_8.name())}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.open_from_clipboard))
            }

            Button(
                onClick = { navController.navigate(Screen.Create.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.manual_entry))
            }
        }
    }
}

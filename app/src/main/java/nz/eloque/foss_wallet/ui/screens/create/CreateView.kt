package nz.eloque.foss_wallet.ui.screens.create

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassCreator
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.ui.screens.settings.ComboBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateView(
    navController: NavHostController,
    createViewModel: CreateViewModel,
) {
    var message by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result == null || result.contents == null) {
                Toast.makeText(context, "Not a valid EmberTalk-Code", Toast.LENGTH_SHORT).show()
            } else {
                message = result.contents
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var name by remember { mutableStateOf("") }
        OutlinedTextField(
            label = { Text(stringResource(R.string.pass_name)) },
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.barcode_value)) },
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(fraction = 0.8f)
            )
            IconButton(
                onClick = {
                    scanLauncher.launch(ScanOptions())
                },
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = stringResource(R.string.scan_barcode)
                )
            }
        }

        var type by remember { mutableStateOf<PassType>(PassType.Generic) }
        ComboBox(
            title = stringResource(R.string.pass_type),
            options = listOf(
                PassType.Generic,
                PassType.StoreCard,
                PassType.Coupon,
                PassType.Event
            ),
            selectedOption = type,
            onOptionSelected = { type = it },
            optionLabel = { context.getString(it.label) },
        )

        val createValid = name.length in 1..<20 && message.isNotEmpty()

        ElevatedButton(
            enabled = createValid,
            onClick = {
                val barCode = BarCode(
                    format = BarcodeFormat.QR_CODE,
                    message = message,
                    encoding = Charsets.UTF_8,
                    altText = message
                )
                val pass = PassCreator.create(name, type, barCode)

                coroutineScope.launch(Dispatchers.IO) {
                    createViewModel.addPass(pass)
                }
                navController.popBackStack()
                navController.navigate("pass/${pass.id}")
            }
        ) {
            Text(stringResource(R.string.create_pass))
        }
        Spacer(modifier = Modifier.imePadding())
    }
}
package nz.eloque.foss_wallet.ui.screens.create

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ImageSearch
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassCreator
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.ui.components.ImagePicker
import nz.eloque.foss_wallet.ui.screens.settings.ComboBox

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateView(
    navController: NavHostController,
    createViewModel: CreateViewModel,
) {
    var logoUrl by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var barcodes by remember {
        mutableStateOf(
            listOf(
                BarcodeDraft(
                    message = "",
                    altText = "",
                    format = BarcodeFormat.QR_CODE,
                )
            )
        )
    }
    var activeBarcodeIndex by remember { mutableStateOf(0) }
    var type by remember { mutableStateOf<PassType>(PassType.Generic) }

    val barCodeModels = barcodes.map {
        BarCode(
            format = it.format,
            message = it.message,
            encoding = Charsets.UTF_8,
            altText = it.altText.ifBlank { it.message },
        )
    }
    val pass = PassCreator.create(name, type, barCodeModels)

    val nameValid = name.length in 1..<30
    val barcodesValid = barcodes.isNotEmpty() && barcodes.zip(barCodeModels).all { (draft, model) ->
        draft.message.isNotEmpty() && barcodeValid(model)
    }
    val createValid = nameValid && barcodesValid && pass != null

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result != null && result.contents != null) {
                if (activeBarcodeIndex !in barcodes.indices) return@rememberLauncherForActivityResult
                barcodes = barcodes.mapIndexed { index, barcode ->
                    if (index != activeBarcodeIndex) {
                        barcode
                    } else {
                        val scannedFormat = try {
                            BarcodeFormat.valueOf(result.formatName)
                        } catch (_: IllegalArgumentException) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.no_barcode_format_given),
                                Toast.LENGTH_SHORT,
                            ).show()
                            BarcodeFormat.QR_CODE
                        }
                        barcode.copy(
                            message = result.contents,
                            altText = result.contents,
                            format = scannedFormat,
                        )
                    }
                }
            }
        }
    )

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { pickedUri ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = ImageScanner.scanFrom(context.contentResolver, pickedUri)
                withContext(Dispatchers.Main) {
                    if (result != null && result.text != null) {
                        if (activeBarcodeIndex !in barcodes.indices) return@withContext
                        barcodes = barcodes.mapIndexed { index, barcode ->
                            if (index != activeBarcodeIndex) {
                                barcode
                            } else {
                                barcode.copy(
                                    message = result.text,
                                    altText = result.text,
                                    format = result.barcodeFormat,
                                )
                            }
                        }
                    } else {
                        Toast.makeText(context, context.getString(R.string.no_barcode_found), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ImagePicker(
            imageUrl = logoUrl,
            onClear = { logoUrl = null },
            onChoose = { logoUrl = it },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            label = { Text(stringResource(R.string.pass_name)) },
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            isError = !nameValid
        )

        barcodes.forEachIndexed { index, barcode ->
            Text(text = "${context.getString(R.string.barcode)} ${index + 1}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.barcode_value)) },
                    value = barcode.message,
                    onValueChange = { value ->
                        barcodes = barcodes.mapIndexed { i, item ->
                            if (i == index) item.copy(message = value) else item
                        }
                    },
                    modifier = Modifier.fillMaxWidth(fraction = 0.72f),
                    isError = barcode.message.isNotEmpty() && !barcodeValid(
                        BarCode(
                            format = barcode.format,
                            message = barcode.message,
                            encoding = Charsets.UTF_8,
                            altText = barcode.altText.ifBlank { barcode.message },
                        )
                    ),
                    supportingText = {
                        if (barcode.message.isNotEmpty() && !barcodeValid(
                                BarCode(
                                    format = barcode.format,
                                    message = barcode.message,
                                    encoding = Charsets.UTF_8,
                                    altText = barcode.altText.ifBlank { barcode.message },
                                )
                            )
                        ) {
                            Text(stringResource(R.string.barcode_value_invalid, barcode.format.toString()))
                        }
                    }
                )

                IconButton(onClick = {
                    activeBarcodeIndex = index
                    pickImageLauncher.launch("image/*")
                }) {
                    Icon(
                        imageVector = Icons.Default.ImageSearch,
                        contentDescription = stringResource(R.string.select_image_with_barcode)
                    )
                }
                IconButton(onClick = {
                    activeBarcodeIndex = index
                    scanLauncher.launch(ScanOptions())
                }) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = stringResource(R.string.scan_barcode)
                    )
                }
                IconButton(
                    enabled = barcodes.size > 1,
                    onClick = {
                        barcodes = barcodes.filterIndexed { i, _ -> i != index }
                        activeBarcodeIndex = activeBarcodeIndex.coerceAtMost(barcodes.lastIndex.coerceAtLeast(0))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }

            OutlinedTextField(
                label = { Text(stringResource(R.string.barcode_alt_text)) },
                value = barcode.altText,
                onValueChange = { value ->
                    barcodes = barcodes.mapIndexed { i, item ->
                        if (i == index) item.copy(altText = value) else item
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            ComboBox(
                title = stringResource(R.string.barcode_format),
                options = BarcodeFormat.entries,
                selectedOption = barcode.format,
                onOptionSelected = { selected ->
                    barcodes = barcodes.mapIndexed { i, item ->
                        if (i == index) item.copy(format = selected) else item
                    }
                },
                optionLabel = { it.name },
            )
        }

        ElevatedButton(
            onClick = {
                barcodes = barcodes + BarcodeDraft(
                    message = "",
                    altText = "",
                    format = BarcodeFormat.QR_CODE,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.add_another_barcode))
        }

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

        ElevatedButton(
            enabled = createValid,
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    createViewModel.addPass(
                        pass = pass!!,
                        iconUrl = logoUrl,
                        logoUrl = logoUrl,
                    )
                }
                navController.popBackStack()
                navController.navigate("pass/${pass!!.id}")
            }
        ) {
            Text(stringResource(R.string.create_pass))
        }
        Spacer(modifier = Modifier.imePadding())
    }
}

private data class BarcodeDraft(
    val message: String,
    val altText: String,
    val format: BarcodeFormat,
)

private fun barcodeValid(barCode: BarCode): Boolean {
    return try {
        barCode.encodeAsBitmap(100, 100, false)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

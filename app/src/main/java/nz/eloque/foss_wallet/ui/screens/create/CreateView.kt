package nz.eloque.foss_wallet.ui.screens.create

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.Pass
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
    passId: String? = null,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val existingPass = if (passId == null) {
        null
    } else {
        createViewModel.passFlowById(passId).collectAsState(initial = null).value
    }

    var loadedPassId by remember { mutableStateOf<String?>(null) }

    var iconUrl by remember { mutableStateOf<Uri?>(null) }
    var logoUrl by remember { mutableStateOf<Uri?>(null) }
    var stripUrl by remember { mutableStateOf<Uri?>(null) }
    var thumbnailUrl by remember { mutableStateOf<Uri?>(null) }
    var footerUrl by remember { mutableStateOf<Uri?>(null) }

    var name by remember { mutableStateOf("") }
    var nameTouched by remember { mutableStateOf(false) }
    var organization by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var logoText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var altText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf<PassType>(PassType.Generic) }
    var format by remember { mutableStateOf(BarcodeFormat.QR_CODE) }
    var isSaving by remember { mutableStateOf(false) }
    var advancedExpanded by remember(passId) { mutableStateOf(passId == null) }

    LaunchedEffect(existingPass?.id) {
        val pass = existingPass ?: return@LaunchedEffect
        if (pass.id == loadedPassId) return@LaunchedEffect

        loadedPassId = pass.id
        name = pass.description
        organization = pass.organization
        serialNumber = pass.serialNumber
        logoText = pass.logoText ?: ""
        type = pass.type

        val barCode = pass.barCodes.firstOrNull()
        message = barCode?.message() ?: ""
        altText = barCode?.altText ?: message
        format = barCode?.format() ?: BarcodeFormat.QR_CODE

        iconUrl = Uri.fromFile(pass.iconFile(context))
        logoUrl = pass.logoFile(context)?.asUri()
        stripUrl = pass.stripFile(context)?.asUri()
        thumbnailUrl = pass.thumbnailFile(context)?.asUri()
        footerUrl = pass.footerFile(context)?.asUri()
    }

    val barCode = BarCode(
        format = format,
        message = message,
        encoding = Charsets.UTF_8,
        altText = altText.ifBlank { message }
    )

    val nameValid = name.length in 1..<30
    val showNameError = nameTouched && !nameValid
    val messageValid = message.isNotEmpty() && barcodeValid(barCode)
    val canSave = nameValid && messageValid && !isSaving

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result != null && result.contents != null) {
                message = result.contents
                altText = result.contents
                try {
                    format = BarcodeFormat.valueOf(result.formatName)
                } catch (_: IllegalArgumentException) {
                    format = BarcodeFormat.QR_CODE
                    Toast.makeText(context, context.getString(R.string.no_barcode_format_given), Toast.LENGTH_SHORT).show()
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
                        message = result.text
                        altText = result.text
                        format = result.barcodeFormat
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
        Text(stringResource(R.string.logo))
        ImagePicker(
            imageUrl = logoUrl,
            onClear = { logoUrl = null },
            onChoose = { logoUrl = it },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            label = { Text(stringResource(R.string.pass_name)) },
            value = name,
            onValueChange = {
                nameTouched = true
                name = it
            },
            modifier = Modifier.fillMaxWidth(),
            isError = showNameError
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
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                isError = !messageValid,
                supportingText = {
                    if (!messageValid) {
                        Text(stringResource(R.string.barcode_value_invalid, format.toString()))
                    }
                }
            )

            IconButton(
                onClick = {
                    pickImageLauncher.launch("image/*")
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ImageSearch,
                    contentDescription = stringResource(R.string.select_image_with_barcode)
                )
            }
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

        ComboBox(
            title = stringResource(R.string.barcode_format),
            options = BarcodeFormat.entries,
            selectedOption = format,
            onOptionSelected = { format = it },
            optionLabel = { it.name },
        )

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

        if (passId != null) {
            ElevatedButton(
                onClick = { advancedExpanded = !advancedExpanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.additional_fields))
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (advancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (advancedExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
                )
            }
        }

        AnimatedVisibility(
            visible = advancedExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.icon))
                ImagePicker(
                    imageUrl = iconUrl,
                    onClear = { iconUrl = null },
                    onChoose = { iconUrl = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.strip))
                ImagePicker(
                    imageUrl = stripUrl,
                    onClear = { stripUrl = null },
                    onChoose = { stripUrl = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.thumbnail))
                ImagePicker(
                    imageUrl = thumbnailUrl,
                    onClear = { thumbnailUrl = null },
                    onChoose = { thumbnailUrl = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.footer))
                ImagePicker(
                    imageUrl = footerUrl,
                    onClear = { footerUrl = null },
                    onChoose = { footerUrl = it },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    label = { Text(stringResource(R.string.organization)) },
                    value = organization,
                    onValueChange = { organization = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    label = { Text(stringResource(R.string.serial_number)) },
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    label = { Text(stringResource(R.string.logo_text)) },
                    value = logoText,
                    onValueChange = { logoText = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    label = { Text(stringResource(R.string.barcode_alt_text)) },
                    value = altText,
                    onValueChange = { altText = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        ElevatedButton(
            enabled = canSave,
            onClick = {
                isSaving = true
                coroutineScope.launch(Dispatchers.IO) {
                    val savedPassId = createViewModel.savePass(
                        existingPass = existingPass,
                        name = name,
                        organization = organization,
                        serialNumber = serialNumber,
                        type = type,
                        format = format,
                        barcodeValue = message,
                        barcodeAltText = altText,
                        logoText = logoText,
                        iconUrl = iconUrl,
                        logoUrl = logoUrl,
                        stripUrl = stripUrl,
                        thumbnailUrl = thumbnailUrl,
                        footerUrl = footerUrl,
                    )
                    withContext(Dispatchers.Main) {
                        isSaving = false
                        navController.popBackStack()
                        navController.navigate("pass/$savedPassId")
                    }
                }
            }
        ) {
            Text(stringResource(if (passId == null) R.string.create_pass else R.string.save_changes))
        }

        Spacer(modifier = Modifier.imePadding())
    }
}

private fun File.asUri(): Uri = Uri.fromFile(this)

private fun barcodeValid(barCode: BarCode): Boolean {
    return try {
        barCode.encodeAsBitmap(100, 100, false)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

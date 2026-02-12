package nz.eloque.foss_wallet.ui.screens.create

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassRelevantDate
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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

    var location by remember { mutableStateOf<Location?>(null) }
    var relevantStart by remember { mutableStateOf<ZonedDateTime?>(null) }
    var relevantEnd by remember { mutableStateOf<ZonedDateTime?>(null) }
    var expirationDate by remember { mutableStateOf<ZonedDateTime?>(null) }

    var backgroundColor by remember { mutableStateOf<Color?>(null) }
    var foregroundColor by remember { mutableStateOf<Color?>(null) }
    var labelColor by remember { mutableStateOf<Color?>(null) }

    var showLocationPicker by remember { mutableStateOf(false) }
    var colorPickerTarget by remember { mutableStateOf<ColorTarget?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var advancedExpanded by remember { mutableStateOf(false) }

    val barCode = BarCode(
        format = format,
        message = message,
        encoding = Charsets.UTF_8,
        altText = altText.ifBlank { message }
    )

    val nameValid = name.length in 1..<30
    val showNameError = nameTouched && !nameValid
    val messageValid = message.isNotEmpty() && barcodeValid(barCode)
    val datesValid = relevantEnd == null || relevantStart != null

    val allColorsBlank = backgroundColor == null && foregroundColor == null && labelColor == null

    val canSave = nameValid && messageValid && datesValid && !isSaving

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

    if (showLocationPicker) {
        LocationPickerDialog(
            createViewModel = createViewModel,
            initial = location,
            onDismiss = { showLocationPicker = false },
            onConfirm = {
                location = it
                showLocationPicker = false
            }
        )
    }

    colorPickerTarget?.let { target ->
        val initial = when (target) {
            ColorTarget.Background -> backgroundColor ?: Color.White
            ColorTarget.Foreground -> foregroundColor ?: Color.White
            ColorTarget.Label -> labelColor ?: Color.White
        }

        ColorPickerDialog(
            title = when (target) {
                ColorTarget.Background -> stringResource(R.string.pass_background_color)
                ColorTarget.Foreground -> stringResource(R.string.pass_foreground_color)
                ColorTarget.Label -> stringResource(R.string.pass_label_color)
            },
            initialColor = initial,
            onDismiss = { colorPickerTarget = null },
            onConfirm = { selected ->
                when (target) {
                    ColorTarget.Background -> backgroundColor = selected.opaque()
                    ColorTarget.Foreground -> foregroundColor = selected.opaque()
                    ColorTarget.Label -> labelColor = selected.opaque()
                }
                colorPickerTarget = null
            }
        )
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

            IconButton(onClick = { pickImageLauncher.launch("image/*") }) {
                Icon(
                    imageVector = Icons.Default.ImageSearch,
                    contentDescription = stringResource(R.string.select_image_with_barcode)
                )
            }
            IconButton(onClick = { scanLauncher.launch(ScanOptions()) }) {
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

                PickableOutlinedField(
                    label = stringResource(R.string.pass_location),
                    value = location?.let { "${it.latitude.formatCoord()}, ${it.longitude.formatCoord()}" }
                        ?: "",
                    onPick = { showLocationPicker = true },
                    onClear = { location = null },
                    clearEnabled = location != null,
                )

                PickableOutlinedField(
                    label = stringResource(R.string.pass_relevant_start),
                    value = relevantStart?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))
                        ?: "",
                    onPick = { openDateTimePicker(context, relevantStart) { relevantStart = it } },
                    onClear = { relevantStart = null },
                    clearEnabled = relevantStart != null,
                )

                PickableOutlinedField(
                    label = stringResource(R.string.pass_relevant_end),
                    value = relevantEnd?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))
                        ?: "",
                    onPick = { openDateTimePicker(context, relevantEnd) { relevantEnd = it } },
                    onClear = { relevantEnd = null },
                    clearEnabled = relevantEnd != null,
                )

                PickableOutlinedField(
                    label = stringResource(R.string.pass_expiration_date),
                    value = expirationDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))
                        ?: "",
                    onPick = { openDateTimePicker(context, expirationDate) { expirationDate = it } },
                    onClear = { expirationDate = null },
                    clearEnabled = expirationDate != null,
                )

                ColorPickerRow(
                    label = stringResource(R.string.pass_background_color),
                    color = backgroundColor,
                    onPick = { colorPickerTarget = ColorTarget.Background },
                    onClear = { backgroundColor = null }
                )

                ColorPickerRow(
                    label = stringResource(R.string.pass_foreground_color),
                    color = foregroundColor,
                    onPick = { colorPickerTarget = ColorTarget.Foreground },
                    onClear = { foregroundColor = null }
                )

                ColorPickerRow(
                    label = stringResource(R.string.pass_label_color),
                    color = labelColor,
                    onPick = { colorPickerTarget = ColorTarget.Label },
                    onClear = { labelColor = null }
                )

            }
        }

        ElevatedButton(
            enabled = canSave,
            onClick = {
                isSaving = true
                coroutineScope.launch(Dispatchers.IO) {
                    val relevantDates = when {
                        relevantStart != null && relevantEnd != null -> listOf(
                            PassRelevantDate.DateInterval(relevantStart!!, relevantEnd!!)
                        )
                        relevantStart != null -> listOf(PassRelevantDate.Date(relevantStart!!))
                        else -> emptyList()
                    }

                    val colors = if (allColorsBlank) {
                        null
                    } else {
                        val fallbackColor = requireNotNull(backgroundColor ?: foregroundColor ?: labelColor)
                        PassColors(
                            background = backgroundColor ?: fallbackColor,
                            foreground = foregroundColor ?: fallbackColor,
                            label = labelColor ?: fallbackColor,
                        )
                    }

                    val savedPassId = createViewModel.savePass(
                        name = name,
                        organization = organization,
                        serialNumber = serialNumber,
                        type = type,
                        format = format,
                        barcodeValue = message,
                        barcodeAltText = altText,
                        logoText = logoText,
                        colors = colors,
                        location = location,
                        relevantDates = relevantDates,
                        expirationDate = expirationDate,
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
            Text(stringResource(R.string.create_pass))
        }

        Spacer(modifier = Modifier.imePadding())
    }
}

@Composable
private fun PickableOutlinedField(
    label: String,
    value: String,
    onPick: () -> Unit,
    onClear: () -> Unit,
    clearEnabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onPick() }
            )
        }
        IconButton(onClick = onClear, enabled = clearEnabled) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.clear_selection)
            )
        }
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    color: Color?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = color?.toHexColor() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                leadingIcon = {
                    Spacer(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color ?: Color.Transparent)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onPick() }
            )
        }
        IconButton(onClick = onClear, enabled = color != null) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.clear_selection)
            )
        }
    }
}

@Composable
private fun ColorPickerDialog(
    title: String,
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
) {
    var envelope by remember {
        mutableStateOf(ColorEnvelope(initialColor.opaque(), initialColor.opaque().toHexColor().drop(1), false))
    }
    val controller = rememberColorPickerController()

    LaunchedEffect(initialColor) {
        controller.selectByColor(initialColor.opaque(), fromUser = false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HsvColorPicker(
                    controller = controller,
                    initialColor = initialColor,
                    onColorChanged = { envelope = it },
                    modifier = Modifier
                        .width(220.dp)
                        .size(220.dp)
                )

                Text(stringResource(R.string.brightness))
                BrightnessSlider(
                    controller = controller,
                    modifier = Modifier
                        .width(220.dp)
                        .size(width = 220.dp, height = 28.dp)
                        .padding(horizontal = 4.dp)
                )

                Text("#${envelope.hexCode}", fontFamily = FontFamily.Monospace)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(envelope.color) }) { Text(stringResource(R.string.save_changes)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) }
        }
    )
}

@Composable
private fun LocationPickerDialog(
    createViewModel: CreateViewModel,
    initial: Location?,
    onDismiss: () -> Unit,
    onConfirm: (Location) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<CreateViewModel.GeocodeResult>>(emptyList()) }
    var selected by remember {
        mutableStateOf(
            initial?.let {
                CreateViewModel.GeocodeResult(
                    displayName = "${it.latitude.formatCoord()}, ${it.longitude.formatCoord()}",
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pass_location)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.location_search_query)) },
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                isSearching = true
                                error = null
                                try {
                                    results = createViewModel.geocode(query)
                                    if (results.isEmpty()) {
                                        error = context.getString(R.string.no_search_results)
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: context.getString(R.string.exception)
                                } finally {
                                    isSearching = false
                                }
                            }
                        },
                        enabled = query.isNotBlank() && !isSearching,
                    ) {
                        Text(if (isSearching) stringResource(R.string.searching) else stringResource(R.string.search))
                    }
                    TextButton(onClick = { selected = null }) {
                        Text(stringResource(R.string.clear_selection))
                    }
                }

                error?.let { Text(it, color = Color.Red) }

                selected?.let {
                    Text(stringResource(R.string.selected_location, it.displayName))
                }

                results.forEach { result ->
                    ElevatedButton(
                        onClick = { selected = result },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(result.displayName)
                    }
                }

                TextButton(
                    onClick = {
                        val geoUri = if (selected != null) {
                            Uri.parse("geo:${selected!!.latitude},${selected!!.longitude}?q=${selected!!.latitude},${selected!!.longitude}")
                        } else {
                            Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                        }
                        val intent = Intent(Intent.ACTION_VIEW, geoUri)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, context.getString(R.string.no_map_app_found), Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = query.isNotBlank() || selected != null,
                ) {
                    Text(stringResource(R.string.open_in_map_app))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selected?.let {
                        val location = Location("").apply {
                            this.latitude = it.latitude
                            this.longitude = it.longitude
                        }
                        onConfirm(location)
                    }
                },
                enabled = selected != null,
            ) {
                Text(stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) }
        }
    )
}

private fun openDateTimePicker(
    context: android.content.Context,
    initial: ZonedDateTime?,
    onPicked: (ZonedDateTime) -> Unit,
) {
    val seed = initial ?: ZonedDateTime.now()
    val calendar = Calendar.getInstance().apply {
        timeInMillis = seed.toInstant().toEpochMilli()
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    onPicked(
                        ZonedDateTime.of(
                            year,
                            month + 1,
                            dayOfMonth,
                            hourOfDay,
                            minute,
                            0,
                            0,
                            ZoneId.systemDefault()
                        )
                    )
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun openDatePicker(
    context: android.content.Context,
    initial: ZonedDateTime?,
    onPicked: (ZonedDateTime) -> Unit,
) {
    val seed = initial ?: ZonedDateTime.now()
    val calendar = Calendar.getInstance().apply {
        timeInMillis = seed.toInstant().toEpochMilli()
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val picked = ZonedDateTime.of(
                year,
                month + 1,
                dayOfMonth,
                0,
                0,
                0,
                0,
                ZoneId.systemDefault()
            )
            onPicked(picked)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private enum class ColorTarget {
    Background,
    Foreground,
    Label,
}

private fun barcodeValid(barCode: BarCode): Boolean {
    return try {
        barCode.encodeAsBitmap(100, 100, false)
        true
    } catch (_: IllegalArgumentException) {
        false
    }
}

private fun Double.formatCoord(): String = String.format("%.6f", this)

private fun Color.toHexColor(): String = String.format("#%06X", this.toArgb() and 0x00FFFFFF)

private fun Color.opaque(): Color = this.copy(alpha = 1f)

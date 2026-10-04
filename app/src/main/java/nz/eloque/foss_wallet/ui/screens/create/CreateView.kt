package nz.eloque.foss_wallet.ui.screens.create

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.compose_kit.input.ComboBox
import nz.eloque.compose_kit.picker.ImagePicker
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.PassCreator
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.ui.Route
import nz.eloque.foss_wallet.ui.screens.scan.ScanContract
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateView(
    navController: NavHostController,
    createViewModel: CreateViewModel,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val coroutineScope = rememberCoroutineScope()

    val form by createViewModel.form.collectAsState()

    fun update(transform: (CreateForm) -> CreateForm) = createViewModel.updateForm(transform)

    var showLocationPicker by rememberSaveable { mutableStateOf(false) }
    var colorPickerTarget by rememberSaveable { mutableStateOf<ColorTarget?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }
    var detailsExpanded by rememberSaveable { mutableStateOf(false) }

    val barCodeModels = form.barcodes.map { it.toBarCode() }
    val pass = PassCreator.create(form.name, form.type, barCodeModels)

    val nameValid = form.name.length in 1..<30
    val showNameError = form.nameTouched && !nameValid
    val barcodesValid =
        form.barcodes.isNotEmpty() &&
            form.barcodes.zip(barCodeModels).all { (draft, model) ->
                draft.message.isNotEmpty() && !model.isNotValid()
            }
    val datesValid = form.relevantEnd == null || form.relevantStart != null
    val createValid = nameValid && barcodesValid && datesValid && pass != null && !isSaving

    val scanLauncher =
        rememberLauncherForActivityResult(ScanContract()) { scanned ->
            if (scanned == null) {
                navController.popBackStack<Route.Wallet>(inclusive = false, saveState = false)
                return@rememberLauncherForActivityResult
            }
            update { f ->
                if (f.activeBarcodeIndex !in f.barcodes.indices) return@update f
                f.copy(
                    barcodes =
                        f.barcodes.mapIndexed { index, barcode ->
                            if (index != f.activeBarcodeIndex) barcode else BarcodeDraft.from(scanned)
                        },
                )
            }
            detailsExpanded = true
        }

    if (showLocationPicker) {
        LocationPickerDialog(
            createViewModel = createViewModel,
            initial = form.location,
            onDismiss = { showLocationPicker = false },
            onConfirm = { picked ->
                update { it.copy(location = picked) }
                showLocationPicker = false
            },
        )
    }

    colorPickerTarget?.let { target ->
        val initial =
            when (target) {
                ColorTarget.Background -> form.backgroundColor ?: Color.White
                ColorTarget.Foreground -> form.foregroundColor ?: Color.White
                ColorTarget.Label -> form.labelColor ?: Color.White
            }

        ColorPickerDialog(
            title =
                when (target) {
                    ColorTarget.Background -> stringResource(R.string.pass_background_color)
                    ColorTarget.Foreground -> stringResource(R.string.pass_foreground_color)
                    ColorTarget.Label -> stringResource(R.string.pass_label_color)
                },
            initialColor = initial,
            onDismiss = { colorPickerTarget = null },
            onConfirm = { selected ->
                val color = selected.opaque()
                update {
                    when (target) {
                        ColorTarget.Background -> it.copy(backgroundColor = color)
                        ColorTarget.Foreground -> it.copy(foregroundColor = color)
                        ColorTarget.Label -> it.copy(labelColor = color)
                    }
                }
                colorPickerTarget = null
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            form.barcodes.forEachIndexed { index, barcode ->
                val isNotValid = barcode.message.isNotEmpty() && barcode.toBarCode().isNotValid()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        label = { Text(stringResource(R.string.barcode_value)) },
                        value = barcode.message,
                        onValueChange = { value ->
                            update { f ->
                                f.copy(barcodes = f.barcodes.mapIndexed { i, item -> if (i == index) item.copy(message = value) else item })
                            }
                        },
                        modifier = Modifier.fillMaxWidth(fraction = 0.72f),
                        isError = isNotValid,
                        supportingText = {
                            if (isNotValid) {
                                Text(stringResource(R.string.barcode_value_invalid, barcode.format.toString()))
                            }
                        },
                    )

                    IconButton(onClick = {
                        update { it.copy(activeBarcodeIndex = index) }
                        scanLauncher.launch(Unit)
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = stringResource(R.string.scan_barcode),
                        )
                    }
                    IconButton(
                        enabled = form.barcodes.size > 1,
                        onClick = {
                            update { f ->
                                val remaining = f.barcodes.filterIndexed { i, _ -> i != index }
                                f.copy(
                                    barcodes = remaining,
                                    activeBarcodeIndex = f.activeBarcodeIndex.coerceAtMost(remaining.lastIndex.coerceAtLeast(0)),
                                )
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                }

                ComboBox(
                    title = stringResource(R.string.barcode_format),
                    options = BarcodeFormat.entries,
                    selectedOption = barcode.format,
                    onOptionSelected = { selected ->
                        update { f ->
                            f.copy(barcodes = f.barcodes.mapIndexed { i, item -> if (i == index) item.copy(format = selected) else item })
                        }
                    },
                    optionLabel = { it.name },
                    onInfo = {
                        val intent =
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://en.wikipedia.org/wiki/Barcode#Types_of_barcodes".toUri(),
                            )
                        context.startActivity(intent)
                    },
                )
            }

            ElevatedButton(
                onClick = {
                    update { it.copy(barcodes = it.barcodes + BarcodeDraft(message = "", altText = "", format = BarcodeFormat.QR_CODE)) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.add_another_barcode))
            }

            if (!detailsExpanded) {
                Button(
                    enabled = barcodesValid,
                    onClick = { detailsExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.continue_to_details))
                }
            }

            AnimatedVisibility(
                visible = detailsExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        label = { Text(stringResource(R.string.pass_name)) },
                        value = form.name,
                        onValueChange = { value -> update { it.copy(name = value, nameTouched = true) } },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showNameError,
                    )

                    ComboBox(
                        title = stringResource(R.string.pass_type),
                        options =
                            listOf(
                                PassType.Generic,
                                PassType.StoreCard,
                                PassType.Coupon,
                                PassType.Event,
                            ),
                        selectedOption = form.type,
                        onOptionSelected = { selected -> update { it.copy(type = selected) } },
                        optionLabel = { resources.getString(it.label) },
                    )

                    ElevatedButton(
                        onClick = { advancedExpanded = !advancedExpanded },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.additional_fields))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (advancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription =
                                if (advancedExpanded) {
                                    stringResource(
                                        R.string.collapse,
                                    )
                                } else {
                                    stringResource(R.string.expand)
                                },
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
                            PickableOutlinedField(
                                label = stringResource(R.string.pass_relevant_start),
                                value =
                                    form.relevantStart?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))
                                        ?: "",
                                leadingIcon = Icons.Default.CalendarToday,
                                onPick = {
                                    openDateTimePicker(
                                        context,
                                        form.relevantStart,
                                    ) { picked -> update { it.copy(relevantStart = picked) } }
                                },
                                onClear = { update { it.copy(relevantStart = null) } },
                                clearEnabled = form.relevantStart != null,
                            )

                            PickableOutlinedField(
                                label = stringResource(R.string.pass_relevant_end),
                                value =
                                    form.relevantEnd?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))
                                        ?: "",
                                leadingIcon = Icons.Default.CalendarToday,
                                onPick = {
                                    openDateTimePicker(
                                        context,
                                        form.relevantEnd,
                                    ) { picked -> update { it.copy(relevantEnd = picked) } }
                                },
                                onClear = { update { it.copy(relevantEnd = null) } },
                                clearEnabled = form.relevantEnd != null,
                            )

                            PickableOutlinedField(
                                label = stringResource(R.string.pass_expiration_date),
                                value =
                                    form.expirationDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))
                                        ?: "",
                                leadingIcon = Icons.Default.CalendarToday,
                                onPick = {
                                    openDateTimePicker(
                                        context,
                                        form.expirationDate,
                                    ) { picked -> update { it.copy(expirationDate = picked) } }
                                },
                                onClear = { update { it.copy(expirationDate = null) } },
                                clearEnabled = form.expirationDate != null,
                            )

                            PickableOutlinedField(
                                label = stringResource(R.string.pass_location),
                                value =
                                    form.location?.let { "${it.latitude.formatCoord()}, ${it.longitude.formatCoord()}" }
                                        ?: "",
                                leadingIcon = Icons.Default.LocationOn,
                                onPick = { showLocationPicker = true },
                                onClear = { update { it.copy(location = null) } },
                                clearEnabled = form.location != null,
                            )

                            ColorPickerRow(
                                label = stringResource(R.string.pass_background_color),
                                icon = Icons.Default.Palette,
                                color = form.backgroundColor,
                                onPick = { colorPickerTarget = ColorTarget.Background },
                                onClear = { update { it.copy(backgroundColor = null) } },
                            )

                            ColorPickerRow(
                                label = stringResource(R.string.pass_foreground_color),
                                icon = Icons.Default.Palette,
                                color = form.foregroundColor,
                                onPick = { colorPickerTarget = ColorTarget.Foreground },
                                onClear = { update { it.copy(foregroundColor = null) } },
                            )

                            ColorPickerRow(
                                label = stringResource(R.string.pass_label_color),
                                icon = Icons.Default.Palette,
                                color = form.labelColor,
                                onPick = { colorPickerTarget = ColorTarget.Label },
                                onClear = { update { it.copy(labelColor = null) } },
                            )

                            OutlinedTextField(
                                label = { Text(stringResource(R.string.organization)) },
                                value = form.organization,
                                onValueChange = { value -> update { it.copy(organization = value) } },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = stringResource(R.string.organization),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedTextField(
                                label = { Text(stringResource(R.string.serial_number)) },
                                value = form.serialNumber,
                                onValueChange = { value -> update { it.copy(serialNumber = value) } },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = stringResource(R.string.serial_number),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            ImagePicker(
                                imageUrl = form.logoUrl,
                                onClear = { update { it.copy(logoUrl = null) } },
                                onChoose = { uri -> update { it.copy(logoUrl = uri) } },
                                label = stringResource(R.string.logo),
                                labelIcon = Icons.Default.Image,
                                modifier = Modifier.fillMaxWidth(),
                                mimeTypes = IMAGE_MIME_TYPES,
                            )

                            ImagePicker(
                                imageUrl = form.iconUrl,
                                onClear = { update { it.copy(iconUrl = null) } },
                                onChoose = { uri -> update { it.copy(iconUrl = uri) } },
                                label = stringResource(R.string.icon),
                                labelIcon = Icons.Default.Image,
                                modifier = Modifier.fillMaxWidth(),
                                mimeTypes = IMAGE_MIME_TYPES,
                            )

                            ImagePicker(
                                imageUrl = form.stripUrl,
                                onClear = { update { it.copy(stripUrl = null) } },
                                onChoose = { uri -> update { it.copy(stripUrl = uri) } },
                                label = stringResource(R.string.strip),
                                labelIcon = Icons.Default.Image,
                                modifier = Modifier.fillMaxWidth(),
                                mimeTypes = IMAGE_MIME_TYPES,
                            )

                            ImagePicker(
                                imageUrl = form.thumbnailUrl,
                                onClear = { update { it.copy(thumbnailUrl = null) } },
                                onChoose = { uri -> update { it.copy(thumbnailUrl = uri) } },
                                label = stringResource(R.string.thumbnail),
                                labelIcon = Icons.Default.Image,
                                modifier = Modifier.fillMaxWidth(),
                                mimeTypes = IMAGE_MIME_TYPES,
                            )

                            ImagePicker(
                                imageUrl = form.footerUrl,
                                onClear = { update { it.copy(footerUrl = null) } },
                                onChoose = { uri -> update { it.copy(footerUrl = uri) } },
                                label = stringResource(R.string.footer),
                                labelIcon = Icons.Default.Image,
                                modifier = Modifier.fillMaxWidth(),
                                mimeTypes = IMAGE_MIME_TYPES,
                            )

                            ImagePicker(
                                imageUrl = form.backgroundUrl,
                                onClear = { update { it.copy(backgroundUrl = null) } },
                                onChoose = { uri -> update { it.copy(backgroundUrl = uri) } },
                                label = stringResource(R.string.background),
                                labelIcon = Icons.Default.Image,
                                modifier = Modifier.fillMaxWidth(),
                                mimeTypes = IMAGE_MIME_TYPES,
                            )
                        }
                    }
                }
            }

            if (detailsExpanded) {
                Button(
                    enabled = createValid,
                    onClick = {
                        isSaving = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val savedPassId = createViewModel.savePass()
                            withContext(Dispatchers.Main) {
                                isSaving = false
                                navController.navigate(Route.Pass(savedPassId)) {
                                    popUpTo<Route.Wallet>()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.create_pass))
                }
                Text(
                    text = stringResource(R.string.created_pass_export_warning),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PickableOutlinedField(
    label: String,
    value: String,
    leadingIcon: ImageVector,
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
                leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = label) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable { onPick() },
            )
        }
        IconButton(onClick = onClear, enabled = clearEnabled) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.clear_selection),
            )
        }
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    icon: ImageVector,
    color: Color?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = color?.toHexColor() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                textStyle =
                    TextStyle(fontFamily = FontFamily.Monospace),
                leadingIcon = {
                    Row(
                        modifier = Modifier.padding(start = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(imageVector = icon, contentDescription = label)
                        Spacer(
                            modifier =
                                Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color ?: Color.Transparent),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable { onPick() },
            )
        }
        IconButton(onClick = onClear, enabled = color != null) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.clear_selection),
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
                    modifier =
                        Modifier
                            .width(220.dp)
                            .size(220.dp),
                )

                Text(stringResource(R.string.brightness))
                BrightnessSlider(
                    controller = controller,
                    modifier =
                        Modifier
                            .width(220.dp)
                            .size(width = 220.dp, height = 28.dp)
                            .padding(horizontal = 4.dp),
                )

                Text("#${envelope.hexCode}", fontFamily = FontFamily.Monospace)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(envelope.color) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) }
        },
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
    val resources = LocalResources.current
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
            },
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
                                        error = resources.getString(R.string.no_search_results)
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: resources.getString(R.string.exception)
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
                        val geoUri =
                            if (selected != null) {
                                "geo:${selected!!.latitude},${selected!!.longitude}?q=${selected!!.latitude},${selected!!.longitude}"
                                    .toUri()
                            } else {
                                "geo:0,0?q=${Uri.encode(query)}".toUri()
                            }
                        val intent = Intent(Intent.ACTION_VIEW, geoUri)
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, resources.getString(R.string.no_map_app_found), Toast.LENGTH_SHORT).show()
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
                        val location =
                            Location("").apply {
                                this.latitude = it.latitude
                                this.longitude = it.longitude
                            }
                        onConfirm(location)
                    }
                },
                enabled = selected != null,
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.back)) }
        },
    )
}

private fun openDateTimePicker(
    context: Context,
    initial: ZonedDateTime?,
    onPicked: (ZonedDateTime) -> Unit,
) {
    val seed = initial ?: ZonedDateTime.now()
    val calendar =
        Calendar.getInstance().apply {
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
                            ZoneId.systemDefault(),
                        ),
                    )
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true,
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH),
    ).show()
}

private enum class ColorTarget {
    Background,
    Foreground,
    Label,
}

private fun Double.formatCoord(): String = String.format(Locale.current.platformLocale, "%.6f", this)

private fun Color.toHexColor(): String = String.format("#%06X", this.toArgb() and 0x00FFFFFF)

private fun Color.opaque(): Color = this.copy(alpha = 1f)

private val IMAGE_MIME_TYPES = arrayOf("image/png", "image/jpeg", "image/svg+xml")
